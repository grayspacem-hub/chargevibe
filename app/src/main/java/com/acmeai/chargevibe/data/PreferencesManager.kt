package com.acmeai.chargevibe.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.chargeVibeDataStore by preferencesDataStore(name = "chargevibe_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val SELECTED_ANIMATION = intPreferencesKey("selected_animation")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SHOW_BATTERY_PERCENT = booleanPreferencesKey("show_battery_percent")
        val AUTO_DISMISS_SECONDS = intPreferencesKey("auto_dismiss_seconds")
        val UNLOCKED_ANIMATIONS = stringSetPreferencesKey("unlocked_animations")
    }

    val overlayEnabled: Flow<Boolean> = context.chargeVibeDataStore.data.map { it[OVERLAY_ENABLED] ?: true }
    val selectedAnimation: Flow<Int> = context.chargeVibeDataStore.data.map { it[SELECTED_ANIMATION] ?: 0 }
    val animationSpeed: Flow<Float> = context.chargeVibeDataStore.data.map { it[ANIMATION_SPEED] ?: 1.0f }
    val soundEnabled: Flow<Boolean> = context.chargeVibeDataStore.data.map { it[SOUND_ENABLED] ?: false }
    val showBatteryPercent: Flow<Boolean> = context.chargeVibeDataStore.data.map { it[SHOW_BATTERY_PERCENT] ?: true }
    val autoDismissSeconds: Flow<Int> = context.chargeVibeDataStore.data.map { it[AUTO_DISMISS_SECONDS] ?: 0 }
    val unlockedAnimations: Flow<Set<String>> = context.chargeVibeDataStore.data.map { it[UNLOCKED_ANIMATIONS] ?: emptySet() }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.chargeVibeDataStore.edit { it[OVERLAY_ENABLED] = enabled }
    }

    suspend fun setSelectedAnimation(index: Int) {
        context.chargeVibeDataStore.edit { it[SELECTED_ANIMATION] = index }
    }

    suspend fun setAnimationSpeed(speed: Float) {
        context.chargeVibeDataStore.edit { it[ANIMATION_SPEED] = speed }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.chargeVibeDataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setShowBatteryPercent(show: Boolean) {
        context.chargeVibeDataStore.edit { it[SHOW_BATTERY_PERCENT] = show }
    }

    suspend fun setAutoDismissSeconds(seconds: Int) {
        context.chargeVibeDataStore.edit { it[AUTO_DISMISS_SECONDS] = seconds }
    }

    suspend fun unlockAnimation(animationId: String) {
        context.chargeVibeDataStore.edit { prefs ->
            val current = prefs[UNLOCKED_ANIMATIONS] ?: emptySet()
            prefs[UNLOCKED_ANIMATIONS] = current + animationId
        }
    }
}
