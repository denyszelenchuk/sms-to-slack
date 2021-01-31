package com.slacksms.app.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(val context: Context) {

    private val prefsName = "smstoslack"
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun save(key: String, text: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(key, text)
        editor.apply()
    }

    fun save(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPref.getString(key, "")
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }
}
