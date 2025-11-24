package com.example.smartfit.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    // --- CONFIGURATION ---
    private const val SENDER_EMAIL = "smartfittt0000@gmail.com" // Put YOUR Gmail here
    private const val SENDER_PASSWORD = "xcoa wysj vuvt xbof" // Put YOUR App Password here (See Step 5)

    suspend fun sendOtpEmail(recipientEmail: String, otpCode: String): Boolean {
        return withContext(Dispatchers.IO) { // Must run in background
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "SmartFit Password Reset Code"
                    setText("Your OTP code is: $otpCode\n\nPlease enter this in the app to reset your password.")
                }

                Transport.send(message)
                true // Success
            } catch (e: Exception) {
                e.printStackTrace()
                false // Failed
            }
        }
    }
}