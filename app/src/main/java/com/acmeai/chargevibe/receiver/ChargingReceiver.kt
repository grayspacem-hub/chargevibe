package com.acmeai.chargevibe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.acmeai.chargevibe.service.ChargingMonitorService

class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Service handles the actual logic; this is a backup receiver
        ChargingMonitorService.start(context)
    }
}
