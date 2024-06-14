package com.example.umeal.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.umeal.R
import java.util.Calendar

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("prefs_manager", Context.MODE_PRIVATE)

    // Default values
    private val defaultExampleBoolean = false
    private val defaultIsExampleLogin = false
    private val defaultToken = ""
    private val defaultName = ""
    private val defaultEmail = ""

    private val calendar = Calendar.getInstance()
    private val defaultHPHTDays = calendar.get(Calendar.YEAR)
    private val defaultHPHTMonth = calendar.get(Calendar.MONTH)
    private val defaultHPHTYear = calendar.get(Calendar.DAY_OF_MONTH)

    var exampleBoolean: Boolean
        get() = sharedPreferences.getBoolean("exampleBoolean", defaultExampleBoolean)
        set(value) = sharedPreferences.edit().putBoolean("exampleBoolean", value).apply()

    var isExampleLogin: Boolean
        get() = sharedPreferences.getBoolean("isExampleLogin", defaultIsExampleLogin)
        set(value) = sharedPreferences.edit().putBoolean("isExampleLogin", value).apply()

    var token: String
        get() = sharedPreferences.getString("token", defaultToken).toString()
        set(value) = sharedPreferences.edit().putString("token", value).apply()
    var name: String
        get() = sharedPreferences.getString("name", defaultName).toString()
        set(value) = sharedPreferences.edit().putString("name", value).apply()
    var email: String
        get() = sharedPreferences.getString("email", defaultEmail).toString()
        set(value) = sharedPreferences.edit().putString("email", value).apply()

    var hphtDays: Int
        get() = sharedPreferences.getInt("hphtdays", defaultHPHTDays)
        set(value) = sharedPreferences.edit().putInt("hphtdays", value).apply()
    var hphtMonth: Int
        get() = sharedPreferences.getInt("hphtmonth", defaultHPHTMonth)
        set(value) = sharedPreferences.edit().putInt("hphtmonth", value).apply()
    var hphtYear: Int
        get() = sharedPreferences.getInt("hphtyear", defaultHPHTYear)
        set(value) = sharedPreferences.edit().putInt("hphtyear", value).apply()

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}