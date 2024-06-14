package com.example.umeal.home.ui.profile

import androidx.lifecycle.ViewModel
import com.example.umeal.utils.daysUntil

class ProfileViewModel() : ViewModel() {
    fun countTrimester(day: Int, month: Int, year: Int): Int {
        val days = daysUntil(year, month, day)
        return when {
            days >= 189 -> 3
            days in 98..188 -> 2
            else -> 1
        }
    }
}