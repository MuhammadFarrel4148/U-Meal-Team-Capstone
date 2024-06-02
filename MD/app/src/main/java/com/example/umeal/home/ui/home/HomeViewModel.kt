package com.example.umeal.home.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.umeal.ResultState

class HomeViewModel : ViewModel() {

    private val _calories = MutableLiveData<Int>().apply {
        value = 1800
    }
    val calories: LiveData<Int> = _calories


    private val _dailyCalorieResult = MutableLiveData<ResultState<Int>>()
    val dailyCalorieResult: LiveData<ResultState<Int>> = _dailyCalorieResult

    fun getUserDailyCalories(trimester: Int) {
        _dailyCalorieResult.postValue(ResultState.Loading)

        val newCalories = when (trimester) {
            1 -> 1800
            2 -> 2200
            3 -> 2400
            else -> {
                _dailyCalorieResult.postValue(ResultState.Error("Trimester tidak valid: $trimester"))
                return
            }
        }
        _dailyCalorieResult.postValue(ResultState.Success(newCalories))
    }
}