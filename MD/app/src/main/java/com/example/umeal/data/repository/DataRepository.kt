package com.example.umeal.data.repository


import com.example.umeal.data.ResultState
import com.example.umeal.data.response.ForgotPasswordResponse
import com.example.umeal.data.response.ResponseLogin
import com.example.umeal.data.response.ResponseRegister
import com.example.umeal.data.retrofit.ApiService
import com.example.umeal.home.ui.scan.ResponseScanImage
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
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
                emit(ResultState.Error(errorResponse.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ResponseLogin::class.java)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    fun register(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Flow<ResultState<ResponseRegister>> = flow {
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

    fun forgotPassword(email: String): Flow<ResultState<ForgotPasswordResponse>> = flow {
        emit(ResultState.Loading())
        try {
            val response = apiService.forgotPassword(email)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ResultState.Success(it))
                } ?: run {
                    emit(ResultState.Error("Empty Response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ForgotPasswordResponse::class.java)
                emit(ResultState.Error(errorResponse.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ForgotPasswordResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    fun changePassword(
        otpCode: String,
        newPassword: String
    ): Flow<ResultState<ForgotPasswordResponse>> = flow {
        emit(ResultState.Loading())
        try {
            val response = apiService.changePassword(codeotp = otpCode, newPassword = newPassword)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ResultState.Success(it))
                } ?: run {
                    emit(ResultState.Error("Empty Response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ForgotPasswordResponse::class.java)
                emit(ResultState.Error(errorResponse.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ForgotPasswordResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }


    fun scanImage(auth: String, file: MultipartBody.Part): Flow<ResultState<ResponseScanImage>> =
        flow {
            emit(ResultState.Loading())
            try {
                val generateToken = generateAuthorization(auth)
                val response = apiService.scanImage(generateToken, file)
                if (response.isSuccessful) {
                    response.body()?.let {
                        emit(ResultState.Success(it))
                    } ?: emit(ResultState.Error("Empty Response"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = errorBody?.let {
                        Gson().fromJson(it, ResponseScanImage::class.java)
                    }
                    val errorMessage = errorResponse?.status?.toString() ?: "Unknown error occurred"
                    emit(ResultState.Error(errorMessage))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = errorBody?.let {
                    Gson().fromJson(it, ResponseScanImage::class.java)
                }
                val errorMessage = errorResponse?.status?.toString() ?: "HTTP Exception"
                emit(ResultState.Error(errorMessage))
            } catch (e: Exception) {
                emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
            }
        }.catch { e ->
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }

    private fun generateAuthorization(token: String): String {
        return "Bearer $token"
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

