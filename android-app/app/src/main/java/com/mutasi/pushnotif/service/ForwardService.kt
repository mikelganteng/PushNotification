package com.mutasi.pushnotif.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mutasi.pushnotif.MainActivity
import com.mutasi.pushnotif.MutasiApp
import com.mutasi.pushnotif.R
import com.mutasi.pushnotif.data.CapturedNotification
import com.mutasi.pushnotif.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ForwardService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SEND -> {
                val id = intent.getStringExtra(EXTRA_NOTIF_ID) ?: return START_NOT_STICKY
                val resend = intent.getBooleanExtra(EXTRA_RESEND, false)
                scope.launch { processById(id, resend) }
            }
            ACTION_RETRY_ALL -> scope.launch { retryAllPending() }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Menunggu notifikasi..."))
    }

    private suspend fun processById(id: String, resend: Boolean) {
        val repo = (application as MutasiApp).repository
        val notif = repo.getAllNotifications().find { it.id == id } ?: return
        sendToServer(notif, resend)
    }

    private suspend fun retryAllPending() {
        val repo = (application as MutasiApp).repository
        repo.getPendingNotifications().forEach { sendToServer(it, false) }
    }

    private fun sendToServer(notif: CapturedNotification, resend: Boolean) {
        val repo = (application as MutasiApp).repository
        val client = ApiClient(repo.getServerUrl(), repo.getApiKey(), repo.getDeviceId())
        val result = client.sendNotification(notif, resend)

        val updated = notif.copy(
            status = if (result.success) "sent" else "failed",
            serverId = result.serverId ?: notif.serverId,
            errorMessage = result.error
        )
        repo.updateNotification(updated)

        updateForeground("${updated.appName}: ${updated.title}")
    }

    private fun updateForeground(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun buildNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mutasi Push Notif Aktif")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Mutasi Forward", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "mutasi_forward"
        private const val NOTIF_ID = 1001
        private const val ACTION_SEND = "com.mutasi.pushnotif.SEND"
        private const val ACTION_RETRY_ALL = "com.mutasi.pushnotif.RETRY_ALL"
        private const val EXTRA_NOTIF_ID = "notif_id"
        private const val EXTRA_RESEND = "resend"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ForwardService::class.java))
        }

        fun sendImmediately(context: Context, notif: CapturedNotification) {
            start(context)
            val intent = Intent(context, ForwardService::class.java).apply {
                action = ACTION_SEND
                putExtra(EXTRA_NOTIF_ID, notif.id)
            }
            context.startService(intent)
        }

        fun resend(context: Context, notifId: String) {
            start(context)
            val intent = Intent(context, ForwardService::class.java).apply {
                action = ACTION_SEND
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_RESEND, true)
            }
            context.startService(intent)
        }

        fun retryAllPending(context: Context) {
            start(context)
            val intent = Intent(context, ForwardService::class.java).apply {
                action = ACTION_RETRY_ALL
            }
            context.startService(intent)
        }
    }
}
