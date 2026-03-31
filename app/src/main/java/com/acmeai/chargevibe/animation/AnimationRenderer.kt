package com.acmeai.chargevibe.animation

import androidx.compose.runtime.Composable
import com.acmeai.chargevibe.data.animations

@Composable
fun RenderAnimation(animationIndex: Int, batteryLevel: Float, speed: Float) {
    val safeIndex = animationIndex.coerceIn(0, animations.size - 1)
    val animId = animations[safeIndex].id

    when (animId) {
        "lightning" -> LightningAnimation(batteryLevel, speed)
        "sakura" -> SakuraAnimation(batteryLevel, speed)
        "dragon" -> DragonFireAnimation(batteryLevel, speed)
        "cyber" -> CyberCircuitAnimation(batteryLevel, speed)
        "ocean" -> OceanWaveAnimation(batteryLevel, speed)
        "galaxy" -> GalaxySpiralAnimation(batteryLevel, speed)
        "neon" -> NeonPulseAnimation(batteryLevel, speed)
        "crystal" -> CrystalGrowthAnimation(batteryLevel, speed)
        "phoenix" -> FlamePhoenixAnimation(batteryLevel, speed)
        "aurora" -> AuroraAnimation(batteryLevel, speed)
        "pixel" -> PixelRainAnimation(batteryLevel, speed)
        "spirit" -> SpiritEnergyAnimation(batteryLevel, speed)
        else -> LightningAnimation(batteryLevel, speed)
    }
}
