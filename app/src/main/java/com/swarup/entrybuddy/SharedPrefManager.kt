package com.swarup.entrybuddy

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private val KEY_ROLE = "role"
    fun saveUserRole(role: String) {
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_ROLE, null)
    }

    fun clearUserRole() {
        editor.remove(KEY_ROLE)
        editor.apply()
    }
}