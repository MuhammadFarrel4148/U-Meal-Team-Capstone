package com.example.umeal.home.ui.scan

import androidx.lifecycle.ViewModel
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody

class ScanImageViewModel(private val dataRepository: DataRepository) : ViewModel() {
        fun scanImage(auth: String, file: MultipartBody.Part) = flow {
            emit(ResultState.Loading())
            try {
                dataRepository.scanImage(auth, file).collect { result ->
                    emit(result)
                }
            } catch (e: Exception) {
                emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
            }
        }.flowOn(Dispatchers.IO)
}