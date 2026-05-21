package com.mutasi.pushnotif

import android.app.Application
import com.mutasi.pushnotif.data.NotificationRepository

class MutasiApp : Application() {
    lateinit var repository: NotificationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = NotificationRepository(this)
    }
}
