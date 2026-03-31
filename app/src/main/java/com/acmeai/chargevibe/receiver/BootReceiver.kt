package com.acmeai.chargevibe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.acmeai.chargevibe.service.ChargingMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ChargingMonitorService.start(context)
        }
    }
}
