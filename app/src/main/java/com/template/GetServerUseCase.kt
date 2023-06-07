package com.template

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.*
import javax.inject.Inject

class GetServerUseCase@Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(packageId:String,userId:String, timeZone: String,getz:String): Flow<Resource<ServerResponse>> =
        flow {
            try {
                emit(Resource.Loading<ServerResponse>())
                val response = repository.getServer(packageId,userId,timeZone,getz)
                emit(Resource.Success<ServerResponse>(response))
            } catch (e: HttpException) {
                emit(
                    Resource.Error<ServerResponse>(
                        e.localizedMessage ?: "Error"
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<ServerResponse>("Check your internet connection."))
            } catch (e: Exception) {
                Log.e("Error","--> ${e.localizedMessage} \n ${e.message}")
                emit(Resource.Error<ServerResponse>("${e.message}"))
            }
        }
}