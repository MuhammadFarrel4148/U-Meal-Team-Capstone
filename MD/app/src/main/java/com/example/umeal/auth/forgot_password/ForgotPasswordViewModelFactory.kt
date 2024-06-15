package com.example.umeal.auth.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.auth.login.ui.LoginViewModel
import com.example.umeal.data.repository.DataRepository

class ForgotPasswordViewModelFactory (private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}