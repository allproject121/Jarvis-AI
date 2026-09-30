package com.example.execution

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactMessageExecutor(private val context: Context) {

    suspend fun sendMessage(
        phoneNumber: String,
        message: String,
        platform: String = "SMS"
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            if (platform.equals("WhatsApp", ignoreCase = true)) {
                val cleanNumber = phoneNumber.replace("+", "").replace("-", "").replace(" ", "")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setPackage("com.whatsapp")
                }
                try {
                    context.startActivity(intent)
                    return@withContext true
                } catch (_: Exception) {
                    // Fallback to standard SMS if WhatsApp isn't installed
                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                        putExtra("sms_body", message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(smsIntent)
                    return@withContext true
                }
            } else {
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                    putExtra("sms_body", message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(smsIntent)
                return@withContext true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Message dispatch: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    suspend fun makeCall(phoneNumber: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Dialer error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
