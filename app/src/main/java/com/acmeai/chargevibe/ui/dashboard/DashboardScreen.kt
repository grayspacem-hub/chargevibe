package com.acmeai.chargevibe.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class BatteryData(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temperature: Float = 0f,
    val voltage: Float = 0f,
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val currentMa: Int = 0,
    val plugType: String = "None",
    val minutesToFull: Int = 0
)

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    var batteryData by remember { mutableStateOf(BatteryData()) }

    LaunchedEffect(Unit) {
        while (true) {
            batteryData = getBatteryData(context)
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Battery Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big battery level card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${batteryData.level}%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (batteryData.isCharging) "⚡ Charging via ${batteryData.plugType}" else "Not Charging",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (batteryData.isCharging && batteryData.minutesToFull > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val hours = batteryData.minutesToFull / 60
                    val mins = batteryData.minutesToFull % 60
                    Text(
                        text = if (hours > 0) "~${hours}h ${mins}m to full" else "~${mins}m to full",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery details grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BatteryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Thermostat,
                label = "Temperature",
                value = "${batteryData.temperature}°C",
                color = if (batteryData.temperature > 40) Color(0xFFFF4444) else MaterialTheme.colorScheme.primary
            )
            BatteryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.ElectricBolt,
                label = "Voltage",
                value = "${batteryData.voltage}V"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BatteryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Speed,
                label = "Current",
                value = "${batteryData.currentMa} mA"
            )
            BatteryStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.HealthAndSafety,
                label = "Health",
                value = batteryData.health,
                color = when (batteryData.health) {
                    "Good" -> Color(0xFF4CAF50)
                    "Overheat" -> Color(0xFFFF4444)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Technology", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(batteryData.technology, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun BatteryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

private fun getBatteryData(context: Context): BatteryData {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
    val currentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

    val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
    val voltage = (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
    val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
    val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
    val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
    val health = when (healthInt) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }
    val plugType = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "None"
    }

    val currentMa = currentNow / 1000
    val minutesToFull = if (isCharging && currentMa > 0 && level < 100) {
        ((100 - level) * 60f / (currentMa.toFloat())).toInt().coerceIn(0, 600)
    } else 0

    return BatteryData(level, isCharging, temp, voltage, health, tech, currentMa, plugType, minutesToFull)
}
