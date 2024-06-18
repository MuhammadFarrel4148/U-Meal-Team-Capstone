package com.example.umeal.home.ui.history

import androidx.lifecycle.ViewModel
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class HistoryViewModel(private val repository: DataRepository) : ViewModel() {
    fun getHistory(
        auth: String,
        id: String,
        day: String,
        month: String,
        year: String
    ) = flow {
        emit(ResultState.Loading())
        try {
            repository.getHistory(auth, id, day, month, year).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}