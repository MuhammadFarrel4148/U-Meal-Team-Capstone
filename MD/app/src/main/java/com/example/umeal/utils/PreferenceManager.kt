package com.example.umeal.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("prefs_manager", Context.MODE_PRIVATE)

    // Default values
    private val defaultExampleBoolean = false
    private val defaultIsExampleLogin = false
    private val defaultToken = ""

    var exampleBoolean: Boolean
        get() = sharedPreferences.getBoolean("exampleBoolean", defaultExampleBoolean)
        set(value) = sharedPreferences.edit().putBoolean("exampleBoolean", value).apply()

    var isExampleLogin: Boolean
        get() = sharedPreferences.getBoolean("isExampleLogin", defaultIsExampleLogin)
        set(value) = sharedPreferences.edit().putBoolean("isExampleLogin", value).apply()

    var token: String
        get() = sharedPreferences.getString("token", defaultToken).toString()
        set(value) = sharedPreferences.edit().putString("token", value).apply()

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}