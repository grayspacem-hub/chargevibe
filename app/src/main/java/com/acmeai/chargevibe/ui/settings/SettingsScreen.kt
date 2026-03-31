package com.acmeai.chargevibe.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.acmeai.chargevibe.ads.AdManager
import com.acmeai.chargevibe.ads.BannerAdView
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.service.ChargingMonitorService
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val overlayEnabled by prefs.overlayEnabled.collectAsState(initial = true)
    val animationSpeed by prefs.animationSpeed.collectAsState(initial = 1.0f)
    val soundEnabled by prefs.soundEnabled.collectAsState(initial = false)
    val showBatteryPercent by prefs.showBatteryPercent.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Charging Overlay Toggle
        SettingsCard(title = "Charging Overlay") {
            SettingsToggle(
                title = "Enable charging animation",
                subtitle = "Show animation when plugged in",
                checked = overlayEnabled,
                onCheckedChange = {
                    scope.launch { prefs.setOverlayEnabled(it) }
                    if (it) ChargingMonitorService.start(context)
                    else ChargingMonitorService.stop(context)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Animation Settings
        SettingsCard(title = "Animation") {
            Column {
                Text(
                    text = "Speed: ${"%.1f".format(animationSpeed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Slider(
                    value = animationSpeed,
                    onValueChange = { scope.launch { prefs.setAnimationSpeed(it) } },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            SettingsToggle(
                title = "Sound effects",
                subtitle = "Play sound when charging starts",
                checked = soundEnabled,
                onCheckedChange = { scope.launch { prefs.setSoundEnabled(it) } }
            )

            SettingsToggle(
                title = "Show battery percentage",
                subtitle = "Display % on charging overlay",
                checked = showBatteryPercent,
                onCheckedChange = { scope.launch { prefs.setShowBatteryPercent(it) } }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Permissions
        SettingsCard(title = "Permissions") {
            Button(
                onClick = {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Disable Battery Optimization")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About
        SettingsCard(title = "About") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ChargeVibe v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Made with ⚡ by ACME AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner ad at bottom
        BannerAdView()
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
