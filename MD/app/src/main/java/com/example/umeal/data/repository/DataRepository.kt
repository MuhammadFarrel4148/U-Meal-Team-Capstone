package com.example.umeal.data.repository

import com.example.umeal.data.ResultState
import com.example.umeal.data.response.ResponseLogin
import com.example.umeal.data.response.ResponseRegister
import com.example.umeal.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

class DataRepository(
    private val apiService: ApiService
) {
    fun login(email: String, password: String): Flow<ResultState<ResponseLogin>> = flow {
        emit(ResultState.Loading())
        try {
            val response = apiService.login(email, password)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ResultState.Success(it))
                } ?: run {
                    emit(ResultState.Error("Empty Response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ResponseLogin::class.java)
                emit(ResultState.Error(errorResponse.status.toString()))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ResponseLogin::class.java)
            emit(ResultState.Error(errorResponse.status.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    fun register(username: String, email: String, phoneNumber: String, password: String): Flow<ResultState<ResponseRegister>> = flow {
        emit(ResultState.Loading())
        try {
            val response = apiService.register(username, email, phoneNumber, password)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ResultState.Success(it))
                } ?: run {
                    emit(ResultState.Error("Empty Response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ResponseRegister::class.java)
                emit(ResultState.Error(errorResponse.status.toString()))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ResponseRegister::class.java)
            emit(ResultState.Error(errorResponse.status.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    companion object {
        @Volatile
        private var instance: DataRepository? = null

        fun getInstance(apiService: ApiService): DataRepository =
            instance ?: synchronized(this) {
                instance ?: DataRepository(apiService).also { instance = it }
            }
    }
}
