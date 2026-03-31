package com.acmeai.chargevibe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.acmeai.chargevibe.ads.AdManager

class ChargeVibeApp : Application() {

    companion object {
        const val CHANNEL_ID = "charging_monitor"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        AdManager.initialize(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Charging Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors charging state for animation overlay"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
