package com.example.umeal.auth.login.ui

import androidx.lifecycle.ViewModel
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LoginViewModel(private val dataRepository: DataRepository) : ViewModel() {

    fun login(email: String, password: String) = flow {
        emit(ResultState.Loading())
        try {
            dataRepository.login(email, password).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}
