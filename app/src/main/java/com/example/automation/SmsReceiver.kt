package com.example.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    companion object {
        var onOtpReceived: ((String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.displayMessageBody
                // Looking for 6-digit OTP like "is: 604043."
                // Example 1: "Your OTP (One Time Password) for 2211092126 is: 604043. Do not share it with anyone. MoRTH"
                // Example 2: "Security code for Slot Booking/Cancellation for Application: 311538.   MoRTH"
                // Wait, example 2 OTP is 6 digits? Let's extract any 6 consecutive digits that are likely the OTP.
                
                val otpRegex = Regex("""\b(\d{6})\b""")
                val match = otpRegex.find(body)
                if (match != null) {
                    val otp = match.groupValues[1]
                    onOtpReceived?.invoke(otp)
                }
            }
        }
    }
}
