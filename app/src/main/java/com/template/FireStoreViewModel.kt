package com.template

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.template.Constants.BASE_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FirestoreViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val getServerUseCase: GetServerUseCase
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _serverUrlState = mutableStateOf("")
    val serverUrlState = _serverUrlState
    private val _stateServerResponse = mutableStateOf<ServerResponseState>(ServerResponseState())
    val stateServerResponse = _stateServerResponse
    init {
        dataStoreManager.getServerUrl.onEach { value ->
            _serverUrlState.value = value
            BASE_URL.value = value
        }.launchIn(viewModelScope)
    }
        fun getFromServer( packageName:String, userId:String, timeZone: String){
            getServerUseCase.invoke(packageName,userId,timeZone,"utm_source=google-play&utm_medium=organic").onEach { result->
                when(result){
                    is Resource.Success->{
                        val response = result.data
                        _stateServerResponse.value = ServerResponseState(response = response)
                        Log.e("TAG","GetServerResponse->\n ${_stateServerResponse.value.response}")
                    }
                    is Resource.Error->{
                        _stateServerResponse.value = ServerResponseState(error = "${result.message}")
                        Log.e("TAG","GetServerError->\n ${result.message} \n ${result.data}")
                    }
                    is Resource.Loading->{
                        _stateServerResponse.value  = ServerResponseState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
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

/*fun sendRequest(url:String,packageName:String,userId:String,timeZone:String){
        Log.e("Request","3")
        val client = OkHttpClient()
        val utmSource = "google-play"
        val utmMedium = "organic"

        val httpUrl = url.toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("packageid", packageName)
            ?.addQueryParameter("usserid", userId)
            ?.addQueryParameter("getz", timeZone)
            ?.addQueryParameter("getr", "utm_source=$utmSource&utm_medium=$utmMedium")
            ?.build()

        val request = httpUrl?.let {
            Log.e("Request","2")
            Request.Builder()
                .url(it)
                .build()
        }

        if (request != null) {
            Log.e("Request","1")
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Failure","${e.message}")
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val responseData = response.body?.string()
                    if(response.isSuccessful) {
                        Log.e("Success", "$responseData \n ${response.message}")
                    }
                    else {
                        Log.e("Error", "${response.message} \n ${response.code} \n ${response.protocol}")
                    }
                    // Обработка ответа
                }
            })
        }
    }*/