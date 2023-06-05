package com.template

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class FirestoreViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _serverUrlState = mutableStateOf("")
    val serverUrlState = _serverUrlState
    init {
        dataStoreManager.getServerUrl.onEach { value ->
            _serverUrlState.value = value
        }.launchIn(viewModelScope)
    }
        fun getFromServer(link:String,packageName:String){
        val retrofit = Retrofit.Builder()
            .baseUrl(link)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(ApplicationApi::class.java)
            val userId = UUID.randomUUID()
        val call = retrofit.getData(packageName, userId.toString(),TimeZone.getDefault().id.toString(), "utm_source=google-play&utm_medium=organic")
        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    val serverResponse = response.body()
                    val url = serverResponse?.url
                    if (response.code() == 200 && url != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            dataStoreManager.updateIsInServer(2)
                            dataStoreManager.updateServerUrl(url)
                        }
                    }
                } else {
                    val status = response.code()
                    if(status == 403){
                        viewModelScope.launch(Dispatchers.IO) {
                            dataStoreManager.updateIsInServer(1)
                        }
                    }
                    Log.e("Error","${response.raw()} \n ${response.message()} \n ${response.body()}")
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.e("Failure","${t.message}")
            }
        })

    }

    fun getLink(): LiveData<String?> {
        val linkData = MutableLiveData<String?>()
        db.collection("database")
            .document("check")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val link = documentSnapshot.getString("link")
                    linkData.value = link
                    Log.e("Success","$link")
                } else {
                    Log.e("Null","$documentSnapshot")
                }
            }
            .addOnFailureListener { exception ->
                linkData.value = exception.message
                Log.e("Error","${exception.message}")
            }
        return linkData
    }
}
