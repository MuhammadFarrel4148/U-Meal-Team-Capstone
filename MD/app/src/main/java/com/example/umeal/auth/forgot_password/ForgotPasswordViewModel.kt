package com.example.umeal.auth.forgot_password

import androidx.lifecycle.ViewModel
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ForgotPasswordViewModel(private val dataRepository: DataRepository) : ViewModel() {
    fun forgotPassword(email: String) = flow {
        emit(ResultState.Loading())
        try {
            dataRepository.forgotPassword(email).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)


    fun changePassword(otpCode: String, newPassword: String) = flow {
        emit(ResultState.Loading())
        try {
            dataRepository.changePassword(otpCode, newPassword).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}