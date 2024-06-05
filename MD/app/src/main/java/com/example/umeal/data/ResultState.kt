package com.example.umeal.data

sealed class ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val error: String) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
}