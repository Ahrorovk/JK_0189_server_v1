package com.template

import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class FirestoreViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _serverUrlState = mutableStateOf("")
    val serverUrlState = _serverUrlState
    init {
        dataStoreManager.getServerUrl.onEach { value ->
            _serverUrlState.value = value
        }.launchIn(viewModelScope)
    }
        fun getFromServer(link:String,packageName:String, userId:String, timeZone: String) {
            val retrofit = Retrofit.Builder()
                .baseUrl(link)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .header("User-Agent", System.getProperty("http.agent"))
                                .build()
                            chain.proceed(request)
                        }
                        .readTimeout(120, TimeUnit.SECONDS)
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .build()
                )
                .build()
                .create(ApplicationApi::class.java)
            val call = retrofit.getData(
                packageName,
                userId,
                timeZone,
                "utm_source=google-play&utm_medium=organic"
            )
            call.enqueue(object : retrofit2.Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if(response.code()==200){
                            viewModelScope.launch(Dispatchers.IO) {
                                dataStoreManager.updateIsInServer(2)
                                dataStoreManager.updateServerUrl(data?:"")
                            }
                        }
                    } else {
                        if(response.code()==403) {
                            viewModelScope.launch(Dispatchers.IO) {
                                dataStoreManager.updateIsInServer(1)
                            }
                        }
                        Log.e("Error","${response.message()} \n ${response.code()} \n ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("Failure","${t.message}")
                }
            })
        }
    fun getLink(): MutableStateFlow<String?> {
        val linkData = MutableStateFlow<String?>("0")
        db.collection("database")
            .document("check")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val link = documentSnapshot.getString("link")
                    if(link == null) {
                        linkData.value = "1"
                        viewModelScope.launch(Dispatchers.IO) {
                            dataStoreManager.updateIsInServer(1)
                        }
                    }
                    else linkData.value = link
                    Log.e("Success","$link \n ${linkData.value}")
                } else {
                    Log.e("Null","$documentSnapshot")
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch(Dispatchers.IO) {
                    dataStoreManager.updateIsInServer(3)
                }
                Log.e("Error","${exception.message}")
            }
        return linkData
    }
}