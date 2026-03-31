package com.acmeai.chargevibe.data

import androidx.compose.ui.graphics.Color

data class AnimationInfo(
    val id: String,
    val name: String,
    val description: String,
    val isPremium: Boolean,
    val primaryColor: Color,
    val secondaryColor: Color
)

val animations = listOf(
    AnimationInfo("lightning", "Electric Lightning", "Crackling energy bolts", false, Color(0xFF00D4FF), Color(0xFF7B2FFF)),
    AnimationInfo("sakura", "Sakura Petals", "Cherry blossoms swirling", false, Color(0xFFFF69B4), Color(0xFFFFB7C5)),
    AnimationInfo("dragon", "Dragon Fire", "Fire dragon breathing energy", false, Color(0xFFFF4500), Color(0xFFFFD700)),
    AnimationInfo("cyber", "Cyber Circuit", "Tron-style flowing circuits", false, Color(0xFF00FF88), Color(0xFF00BFFF)),
    AnimationInfo("ocean", "Ocean Wave", "Crashing waves filling battery", false, Color(0xFF006994), Color(0xFF00CED1)),
    AnimationInfo("galaxy", "Galaxy Spiral", "Cosmic energy vortex", false, Color(0xFF9B30FF), Color(0xFF00BFFF)),
    AnimationInfo("neon", "Neon Pulse", "Retro neon heartbeat", true, Color(0xFFFF00FF), Color(0xFF00FFFF)),
    AnimationInfo("crystal", "Crystal Growth", "Crystals forming with charge", true, Color(0xFF00FFCC), Color(0xFFAA00FF)),
    AnimationInfo("phoenix", "Flame Phoenix", "Phoenix rising with charge", true, Color(0xFFFF6600), Color(0xFFFFCC00)),
    AnimationInfo("aurora", "Aurora Borealis", "Northern lights shimmer", true, Color(0xFF00FF7F), Color(0xFF8A2BE2)),
    AnimationInfo("pixel", "Pixel Rain", "Matrix-style pixel rain", true, Color(0xFF00FF00), Color(0xFF003300)),
    AnimationInfo("spirit", "Spirit Energy", "DBZ-style power aura", true, Color(0xFFFFD700), Color(0xFFFF4500)),
)
