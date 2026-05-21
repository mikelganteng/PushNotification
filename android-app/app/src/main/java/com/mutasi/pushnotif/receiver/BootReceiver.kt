package com.mutasi.pushnotif.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutasi.pushnotif.service.ForwardService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ForwardService.start(context)
        }
    }
}
