package com.example.umeal.utils

import java.util.Calendar
import kotlin.math.abs

fun daysUntil(givenYear: Int, givenMonth: Int, givenDay: Int): Int {
    val today = Calendar.getInstance()
    val targetCalendar = Calendar.getInstance()
    targetCalendar.set(Calendar.YEAR, givenYear)
    targetCalendar.set(Calendar.MONTH, givenMonth - 1) // Months are 0-indexed
    targetCalendar.set(Calendar.DAY_OF_MONTH, givenDay)

    val timeInMillis = targetCalendar.timeInMillis - today.timeInMillis
    val days = (timeInMillis) / (1000 * 60 * 60 * 24)
    return abs(days).toInt()
}