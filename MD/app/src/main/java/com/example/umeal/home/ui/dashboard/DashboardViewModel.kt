package com.example.umeal.home.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.jvm.Throws

class DashboardViewModel : ViewModel() {

    fun countCalorie(
        bb: Double,
        tb: Double,
        age: Double,
        tr: Int,
        facActivity: String,
        facStress: String
    ): Double {
        val bee = 655 + (9.6 * bb) + (1.85 * tb) - (4.68 * age)
        val tee = bee * getFactorValue(facActivity) * getFactorValue(facStress)
        return when (tr) {
            1 -> tee
            2 -> tee + 300
            3 -> tee + 600
            else -> {
                throw IllegalArgumentException("Invalid trimester")
            }
        }
    }

    private fun getFactorValue(string: String): Double {
        val parts = string.split(" - ")
        return parts[1].toDoubleOrNull() ?: 0.0
    }
}