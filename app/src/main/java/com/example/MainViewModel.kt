package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.security.DailyPasswordManager
import com.example.security.LockoutManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val lockoutManager = LockoutManager(application)
    private val passwordManager = DailyPasswordManager()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _lockoutRemainingMs = MutableStateFlow(lockoutManager.getLockoutRemainingMs())
    val lockoutRemainingMs: StateFlow<Long> = _lockoutRemainingMs.asStateFlow()

    private val _timeManipulationDetected = MutableStateFlow(lockoutManager.checkTimeManipulation())
    val timeManipulationDetected: StateFlow<Boolean> = _timeManipulationDetected.asStateFlow()
    
    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    fun verifyPassword(input: String) {
        if (_timeManipulationDetected.value) return
        if (lockoutManager.isLockedOut()) {
            _lockoutRemainingMs.value = lockoutManager.getLockoutRemainingMs()
            return
        }

        val expected = passwordManager.getTodayPassword()
        if (input == expected) {
            lockoutManager.resetAttempts()
            _failedAttempts.value = 0
            _isAuthenticated.value = true
        } else {
            val lockoutUntil = lockoutManager.recordFailedAttempt()
            _failedAttempts.value = _failedAttempts.value + 1
            if (lockoutUntil > 0) {
                _lockoutRemainingMs.value = lockoutManager.getLockoutRemainingMs()
            }
        }
    }
    
    fun updateLockoutTimer() {
        _lockoutRemainingMs.value = lockoutManager.getLockoutRemainingMs()
    }
}
