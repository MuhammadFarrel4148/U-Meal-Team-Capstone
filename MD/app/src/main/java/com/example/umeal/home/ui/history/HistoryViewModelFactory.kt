package com.example.umeal.home.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.data.repository.DataRepository

class HistoryViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}