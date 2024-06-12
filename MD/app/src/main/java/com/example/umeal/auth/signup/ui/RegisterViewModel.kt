package com.example.umeal.auth.signup.ui

import androidx.lifecycle.ViewModel
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RegisterViewModel(private val dataRepository: DataRepository) : ViewModel() {
    fun register(username: String, email: String, phoneNumber: String, password: String) = flow {
        emit(ResultState.Loading())
        try {
            dataRepository.register(username, email, phoneNumber, password).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}