package com.mutasi.pushnotif.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mutasi.pushnotif.MutasiApp
import com.mutasi.pushnotif.data.CapturedNotification
import com.mutasi.pushnotif.parser.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationCaptureService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        ForwardService.start(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val repo = (application as MutasiApp).repository
        if (!repo.isEnabled()) return

        val pkg = sbn.packageName
        if (pkg == applicationContext.packageName) return

        val filters = repo.getFilterPackages()
        if (filters.isNotEmpty() && pkg !in filters) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: body

        val appName = try {
            val pm = packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            pkg
        }

        // Try to parse as transaction
        val transactionInfo = TransactionParser.parseTransaction(pkg, title, body, bigText)

        val captured = CapturedNotification(
            packageName = pkg,
            appName = appName,
            title = title,
            body = body,
            bigText = bigText,
            postedAt = sbn.postTime,
            bankName = transactionInfo?.bankName,
            transactionType = transactionInfo?.transactionType,
            amount = transactionInfo?.amount,
            accountNumber = transactionInfo?.accountNumber,
            senderName = transactionInfo?.senderName,
            isQRIS = transactionInfo?.isQRIS ?: false
        )

        repo.saveNotification(captured)

        scope.launch {
            ForwardService.sendImmediately(applicationContext, captured)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    companion object {
        fun isEnabled(context: Context): Boolean {
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val cn = ComponentName(context, NotificationCaptureService::class.java)
            return flat.split(":").any { it.equals(cn.flattenToString(), ignoreCase = true) }
        }

        fun openSettings(context: Context) {
            context.startActivity(
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
