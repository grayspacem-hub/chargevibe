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
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start charging monitor service
        ChargingMonitorService.start(this)

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

    override fun onResume() {
        super.onResume()
        // App Open ad on resume
        AdManager.showAppOpenAd(this)
    }
}
