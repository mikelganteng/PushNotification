package com.mutasi.pushnotif.network

import com.mutasi.pushnotif.data.CapturedNotification
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiClient(
    private val serverUrl: String,
    private val apiKey: String,
    private val deviceId: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    data class SendResult(val success: Boolean, val serverId: String? = null, val error: String? = null)

    fun sendNotification(notif: CapturedNotification, resend: Boolean = false): SendResult {
        val payload = JSONObject().apply {
            put("package_name", notif.packageName)
            put("app_name", notif.appName)
            put("title", notif.title)
            put("body", notif.body)
            put("big_text", notif.bigText)
            put("posted_at", notif.postedAt)
            put("device_id", deviceId)
            if (resend) {
                put("resend", true)
                put("original_id", notif.serverId ?: notif.id)
            }
        }

        val request = Request.Builder()
            .url("$serverUrl/api/notifications")
            .addHeader("X-API-Key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMedia))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    SendResult(true, json.optString("id", notif.id))
                } else {
                    SendResult(false, error = "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            SendResult(false, error = e.message ?: "Unknown error")
        }
    }

    fun healthCheck(): Boolean {
        val request = Request.Builder()
            .url("$serverUrl/api/health")
            .get()
            .build()
        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }
}
