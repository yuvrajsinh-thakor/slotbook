package com.example.security

import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class DailyPasswordManager {

    private val secretSeed = "MySuperSecretSeed1109"

    fun getPasswordForDate(dateString: String): String {
        val secretKey = SecretKeySpec(secretSeed.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val hash = mac.doFinal(dateString.toByteArray(Charsets.UTF_8))
        
        // Truncate to 8 digits deterministically
        val buffer = ByteBuffer.wrap(hash)
        val intValue = buffer.int.toLong() and 0xFFFFFFFFL // Unsigned 32-bit int
        val eightDigit = intValue % 100000000L
        return String.format(Locale.US, "%08d", eightDigit)
    }

    fun getTodayPassword(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return getPasswordForDate(sdf.format(Date()))
    }
}
