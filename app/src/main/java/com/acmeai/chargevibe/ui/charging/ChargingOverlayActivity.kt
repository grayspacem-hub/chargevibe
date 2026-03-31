package com.acmeai.chargevibe.ui.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acmeai.chargevibe.animation.RenderAnimation
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.ui.theme.ChargeVibeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ChargingOverlayActivity : ComponentActivity() {

    companion object {
        const val ACTION_DISMISS = "com.acmeai.chargevibe.DISMISS_OVERLAY"
    }

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DISMISS) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Register dismiss receiver
        val filter = IntentFilter(ACTION_DISMISS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dismissReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dismissReceiver, filter)
        }

        val prefs = PreferencesManager(this)

        setContent {
            ChargeVibeTheme {
                ChargingOverlayScreen(
                    prefs = prefs,
                    onDismiss = { finish() },
                    getBatteryLevel = { getBatteryLevel() },
                    getChargingInfo = { getChargingInfo() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(dismissReceiver) } catch (_: Exception) {}
    }

    private fun getBatteryLevel(): Float {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100f
    }

    private fun getChargingInfo(): ChargingInfo {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val currentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL

        // Estimate time to full (rough calculation)
        val minutesToFull = if (isCharging && currentNow > 0 && level < 100) {
            val remaining = 100 - level
            (remaining * 60f / (currentNow / 1000f)).toInt().coerceIn(0, 600)
        } else 0

        return ChargingInfo(level, isCharging, currentNow, minutesToFull)
    }
}

data class ChargingInfo(
    val level: Int,
    val isCharging: Boolean,
    val currentMicroAmps: Int,
    val minutesToFull: Int
)

@Composable
fun ChargingOverlayScreen(
    prefs: PreferencesManager,
    onDismiss: () -> Unit,
    getBatteryLevel: () -> Float,
    getChargingInfo: () -> ChargingInfo
) {
    val selectedAnimation by prefs.selectedAnimation.collectAsState(initial = 0)
    val animationSpeed by prefs.animationSpeed.collectAsState(initial = 1.0f)
    val showBatteryPercent by prefs.showBatteryPercent.collectAsState(initial = true)

    var batteryLevel by remember { mutableFloatStateOf(getBatteryLevel()) }
    var chargingInfo by remember { mutableStateOf(getChargingInfo()) }

    // Update battery info periodically
    LaunchedEffect(Unit) {
        while (true) {
            batteryLevel = getBatteryLevel()
            chargingInfo = getChargingInfo()
            kotlinx.coroutines.delay(2000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        // Full-screen animation
        RenderAnimation(
            animationIndex = selectedAnimation,
            batteryLevel = batteryLevel,
            speed = animationSpeed
        )

        // Battery info overlay
        if (showBatteryPercent) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${chargingInfo.level}%",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
                if (chargingInfo.isCharging && chargingInfo.minutesToFull > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val hours = chargingInfo.minutesToFull / 60
                    val mins = chargingInfo.minutesToFull % 60
                    val timeText = if (hours > 0) "${hours}h ${mins}m to full" else "${mins}m to full"
                    Text(
                        text = timeText,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Tap to dismiss",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
