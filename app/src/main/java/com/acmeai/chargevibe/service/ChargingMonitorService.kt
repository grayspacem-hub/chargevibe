package com.acmeai.chargevibe.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.acmeai.chargevibe.ChargeVibeApp
import com.acmeai.chargevibe.R
import com.acmeai.chargevibe.ui.MainActivity
import com.acmeai.chargevibe.ui.charging.ChargingOverlayActivity

class ChargingMonitorService : Service() {

    private var chargingReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        registerChargingReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        chargingReceiver?.let { unregisterReceiver(it) }
    }

    private fun registerChargingReceiver() {
        chargingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        showChargingOverlay()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        dismissChargingOverlay()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(chargingReceiver, filter)
    }

    private fun showChargingOverlay() {
        val intent = Intent(this, ChargingOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun dismissChargingOverlay() {
        val intent = Intent(ChargingOverlayActivity.ACTION_DISMISS).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ChargeVibeApp.CHANNEL_ID)
            .setContentTitle("ChargeVibe Active")
            .setContentText("Monitoring charging state")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChargingMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ChargingMonitorService::class.java))
        }
    }
}
