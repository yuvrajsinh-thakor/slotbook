package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock

class LockoutManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)

    fun recordFailedAttempt(): Long {
        val attempts = prefs.getInt("failed_attempts", 0) + 1
        prefs.edit().putInt("failed_attempts", attempts).apply()

        if (attempts >= 3) {
            val lockoutUntil = System.currentTimeMillis() + (attempts * 10 * 1000) // progressive
            prefs.edit().putLong("lockout_until", lockoutUntil).apply()
            return lockoutUntil
        }
        return 0L
    }

    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong("lockout_until", 0L)
        return System.currentTimeMillis() < lockoutUntil
    }
    
    fun getLockoutRemainingMs(): Long {
        val lockoutUntil = prefs.getLong("lockout_until", 0L)
        val remaining = lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }

    fun resetAttempts() {
        prefs.edit().putInt("failed_attempts", 0).putLong("lockout_until", 0L).apply()
    }

    fun checkTimeManipulation(): Boolean {
        val lastLogin = prefs.getLong("last_login", 0L)
        val current = System.currentTimeMillis()
        if (current < lastLogin - 5000) { // allow 5 sec tolerance
            return true // Time went backward!
        }
        prefs.edit().putLong("last_login", current).apply()
        return false
    }
}
