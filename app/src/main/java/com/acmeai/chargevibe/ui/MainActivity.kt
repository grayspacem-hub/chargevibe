package com.acmeai.chargevibe.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.acmeai.chargevibe.ads.AdManager
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.service.ChargingMonitorService
import com.acmeai.chargevibe.ui.navigation.AppNavigation
import com.acmeai.chargevibe.ui.theme.ChargeVibeTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Start service after permission result (whether granted or not)
        startMonitorService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)

        // Request notification permission for Android 13+, then start service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startMonitorService()
            }
        } else {
            startMonitorService()
        }

        // Load ads
        AdManager.loadAppOpenAd(this)
        AdManager.loadInterstitial(this)
        AdManager.loadRewarded(this)
        AdManager.loadRewardedInterstitial(this)

        setContent {
            ChargeVibeTheme {
                AppNavigation(prefs)
            }
        }
    }

    private fun startMonitorService() {
        try {
            ChargingMonitorService.start(this)
        } catch (e: Exception) {
            // On some devices, foreground service may fail without notification permission
            // The service will start on next app launch when permission is granted
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // App Open ad on resume
        AdManager.showAppOpenAd(this)
    }
}
