package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.DailyPasswordManager
import com.example.security.LockoutManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testDailyPasswordManagerOutput() {
      val manager = DailyPasswordManager()
      val password = manager.getPasswordForDate("2026-08-21")
      
      // Exact 8-digit outputs with leading zeros
      assertTrue("Password must be exactly 8 digits", password.length == 8)
      assertTrue("Password must contain only digits", password.all { it.isDigit() })
  }

  @Test
  fun testSameDateEqualsSamePassword() {
      val manager = DailyPasswordManager()
      val pass1 = manager.getPasswordForDate("2026-08-22")
      val pass2 = manager.getPasswordForDate("2026-08-22")
      assertEquals(pass1, pass2)
  }

  @Test
  fun testDifferentDatesEqualDifferentPasswords() {
      val manager = DailyPasswordManager()
      val pass1 = manager.getPasswordForDate("2026-08-22")
      val pass2 = manager.getPasswordForDate("2026-08-23")
      assertNotEquals(pass1, pass2)
  }
  
  @Test
  fun testExplicitDates() {
      val manager = DailyPasswordManager()
      val dates = listOf("2026-08-21", "2026-08-22", "2026-08-23", "2026-09-01", "2027-01-01")
      val outputs = dates.map { manager.getPasswordForDate(it) }
      
      // All must be 8 digits
      outputs.forEach {
          assertTrue(it.length == 8)
      }
      
      // All must be distinct
      assertEquals(dates.size, outputs.distinct().size)
  }
  
  @Test
  fun testLockoutManager() {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val lockoutManager = LockoutManager(context)
      lockoutManager.resetAttempts()
      
      assertFalse(lockoutManager.isLockedOut())
      
      // 1 failed attempt
      lockoutManager.recordFailedAttempt()
      assertFalse(lockoutManager.isLockedOut())
      
      // 2 failed attempts
      lockoutManager.recordFailedAttempt()
      assertFalse(lockoutManager.isLockedOut())
      
      // 3 failed attempts
      lockoutManager.recordFailedAttempt()
      assertTrue(lockoutManager.isLockedOut())
  }
}
