package com.acmeai.chargevibe.ui.home

import android.content.Context
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acmeai.chargevibe.animation.RenderAnimation
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.data.animations
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    val selectedAnimation by prefs.selectedAnimation.collectAsState(initial = 0)
    val animationSpeed by prefs.animationSpeed.collectAsState(initial = 1.0f)

    var batteryLevel by remember { mutableIntStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            isCharging = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
            delay(3000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ChargeVibe",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Battery status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isCharging) "⚡ Charging" else "Not charging",
                        color = if (isCharging) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Active Animation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = animations.getOrNull(selectedAnimation)?.name ?: "Lightning",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animation preview
        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                RenderAnimation(
                    animationIndex = selectedAnimation,
                    batteryLevel = batteryLevel / 100f,
                    speed = animationSpeed
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Plug in your charger to see it in action!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
