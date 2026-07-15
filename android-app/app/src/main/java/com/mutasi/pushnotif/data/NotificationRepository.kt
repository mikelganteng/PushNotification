package com.mutasi.pushnotif.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CapturedNotification(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val title: String,
    val body: String,
    val bigText: String,
    val postedAt: Long,
    var status: String = "pending",
    var serverId: String? = null,
    var errorMessage: String? = null,
    // Transaction fields
    val bankName: String? = null,
    val transactionType: String? = null,
    val amount: Double? = null,
    val accountNumber: String? = null,
    val senderName: String? = null,
    val isQRIS: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("package_name", packageName)
        put("app_name", appName)
        put("title", title)
        put("body", body)
        put("big_text", bigText)
        put("posted_at", postedAt)
        put("status", status)
        put("server_id", serverId)
        put("error_message", errorMessage)
        put("bank_name", bankName)
        put("transaction_type", transactionType)
        put("amount", amount)
        put("account_number", accountNumber)
        put("sender_name", senderName)
        put("is_qris", isQRIS)
    }

    companion object {
        fun fromJson(obj: JSONObject) = CapturedNotification(
            id = obj.optString("id"),
            packageName = obj.optString("package_name"),
            appName = obj.optString("app_name"),
            title = obj.optString("title"),
            body = obj.optString("body"),
            bigText = obj.optString("big_text"),
            postedAt = obj.optLong("posted_at"),
            status = obj.optString("status", "pending"),
            serverId = obj.optString("server_id").ifEmpty { null },
            errorMessage = obj.optString("error_message").ifEmpty { null },
            bankName = obj.optString("bank_name").ifEmpty { null },
            transactionType = obj.optString("transaction_type").ifEmpty { null },
            amount = if (obj.has("amount")) obj.optDouble("amount") else null,
            accountNumber = obj.optString("account_number").ifEmpty { null },
            senderName = obj.optString("sender_name").ifEmpty { null },
            isQRIS = obj.optBoolean("is_qris", false)
        )
    }
}

class NotificationRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mutasi_notif", Context.MODE_PRIVATE)

    fun getServerUrl(): String =
        prefs.getString("server_url", "http://192.168.1.100:3000") ?: "http://192.168.1.100:3000"

    fun setServerUrl(url: String) = prefs.edit().putString("server_url", url.trim()).apply()

    fun getApiKey(): String =
        prefs.getString("api_key", "mutasi-secret-key") ?: "mutasi-secret-key"

    fun setApiKey(key: String) = prefs.edit().putString("api_key", key.trim()).apply()

    fun getDeviceId(): String {
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("device_id", id).apply()
        }
        return id
    }

    fun isEnabled(): Boolean = prefs.getBoolean("enabled", true)

    fun setEnabled(enabled: Boolean) = prefs.edit().putBoolean("enabled", enabled).apply()

    fun getFilterPackages(): Set<String> {
        val raw = prefs.getString("filter_packages", "") ?: ""
        return if (raw.isBlank()) emptySet()
        else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun setFilterPackages(packages: Set<String>) =
        prefs.edit().putString("filter_packages", packages.joinToString(",")).apply()

    fun saveNotification(notif: CapturedNotification) {
        val list = getAllNotifications().toMutableList()
        list.add(0, notif)
        val trimmed = list.take(500)
        saveList(trimmed)
    }

    fun updateNotification(notif: CapturedNotification) {
        val list = getAllNotifications().toMutableList()
        val idx = list.indexOfFirst { it.id == notif.id }
        if (idx >= 0) {
            list[idx] = notif
            saveList(list)
        }
    }

    fun getAllNotifications(): List<CapturedNotification> {
        val raw = prefs.getString("notifications", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { CapturedNotification.fromJson(arr.getJSONObject(it)) }
    }

    fun getPendingNotifications(): List<CapturedNotification> =
        getAllNotifications().filter { it.status == "pending" || it.status == "failed" }

    private fun saveList(list: List<CapturedNotification>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("notifications", arr.toString()).apply()
    }
}
