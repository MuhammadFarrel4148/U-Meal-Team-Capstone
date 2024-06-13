package com.example.umeal.home.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.data.repository.DataRepository

class ScanImageViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanImageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}