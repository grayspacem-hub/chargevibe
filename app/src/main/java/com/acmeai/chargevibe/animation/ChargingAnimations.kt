package com.acmeai.chargevibe.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import kotlin.math.*
import kotlin.random.Random

// ============================================================
// 1. ELECTRIC LIGHTNING BOLT
// ============================================================
@Composable
fun LightningAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightning")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((1000 / speed).toInt(), easing = LinearEasing)),
        label = "phase"
    )
    val flash by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((300 / speed).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flash"
    )

    val bolts = remember { List(8) { generateBoltPoints() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark background with electric glow
        drawRect(Color(0xFF050520))

        // Central glow
        val glowRadius = w * 0.6f * (0.5f + batteryLevel * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x6600D4FF), Color(0x337B2FFF), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(w / 2, h / 2)
        )

        // Lightning bolts
        val activeBolts = (batteryLevel * 8).toInt().coerceIn(2, 8)
        for (i in 0 until activeBolts) {
            val boltPhase = (phase + i * 0.125f) % 1f
            val alpha = (sin(boltPhase * PI * 2).toFloat() * 0.5f + 0.5f) * (0.4f + batteryLevel * 0.6f)
            val bolt = bolts[i]

            val path = Path()
            bolt.forEachIndexed { idx, point ->
                val x = w * 0.2f + point.first * w * 0.6f
                val y = h * 0.1f + point.second * h * 0.8f
                val jitter = sin((boltPhase + idx * 0.3f) * PI * 2).toFloat() * w * 0.02f
                if (idx == 0) path.moveTo(x + jitter, y)
                else path.lineTo(x + jitter, y)
            }

            // Glow
            drawPath(path, Color(0xFF00D4FF).copy(alpha = alpha * 0.3f),
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Core
            drawPath(path, Color.White.copy(alpha = alpha),
                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        // Flash overlay
        if (flash > 0.9f && Random.nextFloat() > 0.7f) {
            drawRect(Color.White.copy(alpha = 0.05f))
        }
    }
}

private fun generateBoltPoints(): List<Pair<Float, Float>> {
    val points = mutableListOf<Pair<Float, Float>>()
    var y = 0f
    val segments = Random.nextInt(5, 10)
    for (i in 0..segments) {
        val x = 0.3f + Random.nextFloat() * 0.4f
        points.add(Pair(x, y))
        y += 1f / segments
    }
    return points
}

// ============================================================
// 2. SAKURA PETALS
// ============================================================
@Composable
fun SakuraAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "sakura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((8000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    data class Petal(val x: Float, val y: Float, val size: Float, val rotation: Float, val speed: Float, val drift: Float)
    val petals = remember {
        List(40) {
            Petal(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 0.02f + 0.01f,
                rotation = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 0.3f + 0.2f,
                drift = Random.nextFloat() * 0.1f - 0.05f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Soft pink-purple gradient background
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF1A0A1E), Color(0xFF2D1B35), Color(0xFF0D0515))))

        // Central cherry blossom glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x44FF69B4), Color(0x22FFB7C5), Color.Transparent),
                center = Offset(w / 2, h * 0.4f),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(w / 2, h * 0.4f)
        )

        val activePetals = (batteryLevel * 40).toInt().coerceIn(10, 40)
        petals.take(activePetals).forEach { petal ->
            val t = time / 360f
            val py = ((petal.y + t * petal.speed) % 1.2f) - 0.1f
            val px = petal.x + sin((t * 360 + petal.rotation).toDouble() * PI / 180).toFloat() * petal.drift
            val rot = time * petal.speed + petal.rotation

            rotate(rot, pivot = Offset(px * w, py * h)) {
                val petalSize = petal.size * w
                val petalPath = Path().apply {
                    moveTo(px * w, py * h - petalSize)
                    cubicTo(px * w + petalSize * 0.8f, py * h - petalSize * 0.5f,
                        px * w + petalSize * 0.5f, py * h + petalSize * 0.3f,
                        px * w, py * h + petalSize * 0.5f)
                    cubicTo(px * w - petalSize * 0.5f, py * h + petalSize * 0.3f,
                        px * w - petalSize * 0.8f, py * h - petalSize * 0.5f,
                        px * w, py * h - petalSize)
                }
                drawPath(petalPath, Color(0xFFFF69B4).copy(alpha = 0.7f))
                drawPath(petalPath, Color(0xFFFFB7C5).copy(alpha = 0.3f),
                    style = Stroke(width = 1f))
            }
        }
    }
}

// ============================================================
// 3. DRAGON FIRE
// ============================================================
@Composable
fun DragonFireAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "dragon")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((2000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    data class Flame(val x: Float, val baseY: Float, val width: Float, val height: Float, val phase: Float)
    val flames = remember {
        List(30) {
            Flame(
                x = Random.nextFloat(),
                baseY = 0.7f + Random.nextFloat() * 0.3f,
                width = Random.nextFloat() * 0.08f + 0.03f,
                height = Random.nextFloat() * 0.4f + 0.2f,
                phase = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF0A0000))

        // Fire glow from bottom
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x33FF4500), Color(0x66FF6600), Color(0x99FF4500)),
                startY = h * 0.3f, endY = h
            )
        )

        val activeFlames = (batteryLevel * 30).toInt().coerceIn(8, 30)
        flames.take(activeFlames).forEach { flame ->
            val t = (time + flame.phase) % 1f
            val flameHeight = flame.height * (0.5f + batteryLevel * 0.5f)
            val flickerX = sin((t * 360 + flame.phase * 720).toDouble() * PI / 180).toFloat() * 0.02f
            val flickerH = sin((t * 540 + flame.phase * 360).toDouble() * PI / 180).toFloat() * 0.1f

            val cx = (flame.x + flickerX) * w
            val bottom = flame.baseY * h
            val top = bottom - (flameHeight + flickerH) * h
            val fw = flame.width * w

            val path = Path().apply {
                moveTo(cx - fw, bottom)
                cubicTo(cx - fw * 0.5f, bottom - (bottom - top) * 0.3f,
                    cx - fw * 0.3f, top + (bottom - top) * 0.2f,
                    cx, top)
                cubicTo(cx + fw * 0.3f, top + (bottom - top) * 0.2f,
                    cx + fw * 0.5f, bottom - (bottom - top) * 0.3f,
                    cx + fw, bottom)
                close()
            }

            // Outer glow
            drawPath(path, Color(0xFFFF4500).copy(alpha = 0.3f))
            // Inner bright
            val innerPath = Path().apply {
                moveTo(cx - fw * 0.5f, bottom)
                cubicTo(cx - fw * 0.3f, bottom - (bottom - top) * 0.4f,
                    cx - fw * 0.15f, top + (bottom - top) * 0.3f,
                    cx, top + (bottom - top) * 0.15f)
                cubicTo(cx + fw * 0.15f, top + (bottom - top) * 0.3f,
                    cx + fw * 0.3f, bottom - (bottom - top) * 0.4f,
                    cx + fw * 0.5f, bottom)
                close()
            }
            drawPath(innerPath, Color(0xFFFFD700).copy(alpha = 0.6f))
        }
    }
}

// ============================================================
// 4. CYBER CIRCUIT
// ============================================================
@Composable
fun CyberCircuitAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((3000 / speed).toInt(), easing = LinearEasing)),
        label = "pulse"
    )

    data class CircuitLine(val startX: Float, val startY: Float, val segments: List<Pair<Float, Float>>)
    val circuits = remember {
        List(15) {
            val segs = mutableListOf<Pair<Float, Float>>()
            var x = Random.nextFloat()
            var y = Random.nextFloat()
            repeat(Random.nextInt(3, 8)) {
                if (Random.nextBoolean()) x += (Random.nextFloat() - 0.5f) * 0.2f
                else y += (Random.nextFloat() - 0.5f) * 0.15f
                segs.add(Pair(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f)))
            }
            CircuitLine(segs.first().first, segs.first().second, segs)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF000A0A))

        // Grid lines
        val gridAlpha = 0.05f + batteryLevel * 0.05f
        for (i in 0..20) {
            val x = w * i / 20f
            drawLine(Color(0xFF00FF88).copy(alpha = gridAlpha), Offset(x, 0f), Offset(x, h), strokeWidth = 0.5f)
            val y = h * i / 20f
            drawLine(Color(0xFF00FF88).copy(alpha = gridAlpha), Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
        }

        val activeCircuits = (batteryLevel * 15).toInt().coerceIn(4, 15)
        circuits.take(activeCircuits).forEachIndexed { idx, circuit ->
            val linePhase = (pulse + idx * 0.067f) % 1f
            val path = Path()
            circuit.segments.forEachIndexed { i, seg ->
                val sx = seg.first * w
                val sy = seg.second * h
                if (i == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
            }

            // Dim base line
            drawPath(path, Color(0xFF00FF88).copy(alpha = 0.1f),
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Traveling pulse — use segment interpolation
            val totalSegs = circuit.segments.size
            if (totalSegs >= 2) {
                val segFloat = linePhase * (totalSegs - 1)
                val segIdx = segFloat.toInt().coerceIn(0, totalSegs - 2)
                val segFrac = segFloat - segIdx
                val s1 = circuit.segments[segIdx]
                val s2 = circuit.segments[segIdx + 1]
                val px = (s1.first + (s2.first - s1.first) * segFrac) * w
                val py = (s1.second + (s2.second - s1.second) * segFrac) * h

                drawCircle(Color(0xFF00FF88), radius = 4f + batteryLevel * 4f,
                    center = Offset(px, py))
                drawCircle(Color(0xFF00FF88).copy(alpha = 0.3f), radius = 12f + batteryLevel * 8f,
                    center = Offset(px, py))
            }

            // Nodes at segment ends
            circuit.segments.forEach { seg ->
                drawCircle(Color(0xFF00BFFF).copy(alpha = 0.5f + sin((pulse * 360 + idx * 30f).toDouble() * PI / 180).toFloat() * 0.3f),
                    radius = 3f, center = Offset(seg.first * w, seg.second * h))
            }
        }
    }
}

// ============================================================
// 5. OCEAN WAVE
// ============================================================
@Composable
fun OceanWaveAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((4000 / speed).toInt(), easing = LinearEasing)),
        label = "wave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep ocean background
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF000A14), Color(0xFF001428), Color(0xFF002942))))

        // Water level based on battery
        val waterLevel = h * (1f - batteryLevel * 0.85f)

        // Multiple wave layers
        for (layer in 0..3) {
            val amplitude = (20f + layer * 8f) * (0.5f + batteryLevel * 0.5f)
            val frequency = 0.008f + layer * 0.003f
            val phaseOffset = layer * 45f
            val alpha = 0.3f - layer * 0.05f

            val wavePath = Path().apply {
                moveTo(0f, h)
                for (x in 0..w.toInt() step 4) {
                    val xf = x.toFloat()
                    val angle = (xf * frequency * 360 + wavePhase * speed + phaseOffset).toDouble() * PI / 180
                    val y = waterLevel + layer * 15f + sin(angle).toFloat() * amplitude
                    if (x == 0) moveTo(xf, y) else lineTo(xf, y)
                }
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            val color = when (layer) {
                0 -> Color(0xFF006994)
                1 -> Color(0xFF00839B)
                2 -> Color(0xFF00A5B3)
                else -> Color(0xFF00CED1)
            }
            drawPath(wavePath, color.copy(alpha = alpha + 0.2f))
        }

        // Foam on wave crests
        for (x in 0..w.toInt() step 20) {
            val xf = x.toFloat()
            val angle = (xf * 0.008f * 360 + wavePhase * speed).toDouble() * PI / 180
            val y = waterLevel + sin(angle).toFloat() * 20f
            val foamAlpha = (sin(angle).toFloat() * 0.5f + 0.5f) * 0.4f
            drawCircle(Color.White.copy(alpha = foamAlpha), radius = 3f, center = Offset(xf, y))
        }
    }
}

// ============================================================
// 6. GALAXY SPIRAL
// ============================================================
@Composable
fun GalaxySpiralAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((10000 / speed).toInt(), easing = LinearEasing)),
        label = "rotation"
    )

    data class Star(val angle: Float, val distance: Float, val size: Float, val brightness: Float, val armOffset: Float)
    val stars = remember {
        List(200) {
            Star(
                angle = Random.nextFloat() * 360f,
                distance = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                brightness = Random.nextFloat(),
                armOffset = (Random.nextFloat() - 0.5f) * 30f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val maxR = minOf(w, h) * 0.45f

        drawRect(Color(0xFF020010))

        // Core glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xAA9B30FF), Color(0x4400BFFF), Color.Transparent),
                center = Offset(cx, cy), radius = maxR * 0.3f
            ),
            radius = maxR * 0.3f, center = Offset(cx, cy)
        )

        val activeStars = (batteryLevel * 200).toInt().coerceIn(40, 200)
        stars.take(activeStars).forEach { star ->
            val spiralAngle = star.angle + star.distance * 720f + rotation + star.armOffset
            val r = star.distance * maxR
            val rad = spiralAngle.toDouble() * PI / 180
            val sx = cx + cos(rad).toFloat() * r
            val sy = cy + sin(rad).toFloat() * r

            val twinkle = (sin((rotation * 2 + star.angle).toDouble() * PI / 180).toFloat() * 0.3f + 0.7f)
            val color = if (star.distance < 0.3f) Color(0xFF9B30FF) else Color(0xFF00BFFF)

            drawCircle(color.copy(alpha = star.brightness * twinkle * 0.7f),
                radius = star.size + batteryLevel * 2f, center = Offset(sx.toFloat(), sy.toFloat()))
        }
    }
}

// ============================================================
// 7. NEON PULSE (Premium)
// ============================================================
@Composable
fun NeonPulseAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((1500 / speed).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((5000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        drawRect(Color(0xFF05000A))

        // Pulsing rings
        val ringCount = (batteryLevel * 8).toInt().coerceIn(3, 8)
        for (i in 0 until ringCount) {
            val ringPhase = (pulse + i * 0.1f) % 1f
            val radius = (50f + i * 40f) * ringPhase * (0.5f + batteryLevel * 0.5f)
            val alpha = (1f - ringPhase) * 0.6f
            val color = if (i % 2 == 0) Color(0xFFFF00FF) else Color(0xFF00FFFF)

            drawCircle(color.copy(alpha = alpha), radius = radius, center = Offset(cx, cy),
                style = Stroke(width = 3f + (1f - ringPhase) * 4f))
        }

        // Neon heartbeat line
        val path = Path()
        for (x in 0..w.toInt() step 2) {
            val xf = x.toFloat()
            val normalX = (xf / w - 0.5f) * 2f
            val heartbeat = if (abs(normalX) < 0.3f) {
                sin((xf * 0.05f + time).toDouble() * PI / 180).toFloat() * 100f * batteryLevel * pulse
            } else {
                sin((xf * 0.02f + time * 0.5f).toDouble() * PI / 180).toFloat() * 20f
            }
            val y = cy + heartbeat
            if (x == 0) path.moveTo(xf, y) else path.lineTo(xf, y)
        }
        drawPath(path, Color(0xFFFF00FF).copy(alpha = 0.3f), style = Stroke(width = 6f, cap = StrokeCap.Round))
        drawPath(path, Color(0xFF00FFFF), style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}

// ============================================================
// 8. CRYSTAL GROWTH (Premium)
// ============================================================
@Composable
fun CrystalGrowthAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "crystal")
    val growth by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((6000 / speed).toInt(), easing = LinearEasing)),
        label = "growth"
    )

    data class Crystal(val x: Float, val baseY: Float, val height: Float, val width: Float, val angle: Float, val hue: Float)
    val crystals = remember {
        List(20) {
            Crystal(
                x = Random.nextFloat(),
                baseY = 0.6f + Random.nextFloat() * 0.4f,
                height = Random.nextFloat() * 0.25f + 0.1f,
                width = Random.nextFloat() * 0.04f + 0.015f,
                angle = (Random.nextFloat() - 0.5f) * 30f,
                hue = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF050510))

        val activeCrystals = (batteryLevel * 20).toInt().coerceIn(5, 20)
        crystals.take(activeCrystals).forEachIndexed { idx, crystal ->
            val crystalGrowth = ((growth + idx * 0.05f) % 1f).coerceIn(0f, 1f)
            val crystalH = crystal.height * h * crystalGrowth * (0.5f + batteryLevel * 0.5f)
            val crystalW = crystal.width * w

            val baseX = crystal.x * w
            val baseY = crystal.baseY * h

            rotate(crystal.angle, pivot = Offset(baseX, baseY)) {
                val path = Path().apply {
                    moveTo(baseX, baseY)
                    lineTo(baseX - crystalW, baseY - crystalH * 0.6f)
                    lineTo(baseX, baseY - crystalH)
                    lineTo(baseX + crystalW, baseY - crystalH * 0.6f)
                    close()
                }

                val shimmer = sin((growth * 360 + idx * 45f).toDouble() * PI / 180).toFloat() * 0.3f + 0.7f
                val color = if (crystal.hue < 0.5f) Color(0xFF00FFCC) else Color(0xFFAA00FF)

                // Crystal body
                drawPath(path, color.copy(alpha = 0.4f * shimmer))
                // Crystal edge glow
                drawPath(path, color.copy(alpha = 0.7f * shimmer),
                    style = Stroke(width = 1.5f))
                // Inner highlight
                val innerPath = Path().apply {
                    moveTo(baseX, baseY - crystalH * 0.1f)
                    lineTo(baseX - crystalW * 0.3f, baseY - crystalH * 0.5f)
                    lineTo(baseX, baseY - crystalH * 0.85f)
                    lineTo(baseX + crystalW * 0.3f, baseY - crystalH * 0.5f)
                    close()
                }
                drawPath(innerPath, Color.White.copy(alpha = 0.15f * shimmer))
            }
        }

        // Ground glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x2200FFCC), Color(0x44AA00FF)),
                startY = h * 0.7f, endY = h
            )
        )
    }
}

// ============================================================
// 9. FLAME PHOENIX (Premium)
// ============================================================
@Composable
fun FlamePhoenixAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "phoenix")
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((1200 / speed).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wingFlap"
    )
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((3000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h * 0.45f

        drawRect(Color(0xFF0A0000))

        // Rising heat distortion
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x22FF4500), Color(0x44FF6600)),
                startY = 0f, endY = h
            )
        )

        val wingSpan = w * 0.35f * (0.6f + batteryLevel * 0.4f)
        val wingHeight = h * 0.15f

        // Left wing
        val leftWing = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx - wingSpan * 0.3f, cy - wingHeight * wingFlap,
                cx - wingSpan * 0.7f, cy - wingHeight * 1.5f * wingFlap,
                cx - wingSpan, cy - wingHeight * wingFlap * 0.5f)
            cubicTo(cx - wingSpan * 0.5f, cy + wingHeight * 0.3f,
                cx - wingSpan * 0.2f, cy + wingHeight * 0.1f,
                cx, cy)
        }

        // Right wing
        val rightWing = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx + wingSpan * 0.3f, cy - wingHeight * wingFlap,
                cx + wingSpan * 0.7f, cy - wingHeight * 1.5f * wingFlap,
                cx + wingSpan, cy - wingHeight * wingFlap * 0.5f)
            cubicTo(cx + wingSpan * 0.5f, cy + wingHeight * 0.3f,
                cx + wingSpan * 0.2f, cy + wingHeight * 0.1f,
                cx, cy)
        }

        // Wing glow
        drawPath(leftWing, Color(0xFFFF6600).copy(alpha = 0.4f))
        drawPath(rightWing, Color(0xFFFF6600).copy(alpha = 0.4f))
        drawPath(leftWing, Color(0xFFFFCC00).copy(alpha = 0.6f), style = Stroke(width = 3f))
        drawPath(rightWing, Color(0xFFFFCC00).copy(alpha = 0.6f), style = Stroke(width = 3f))

        // Body glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xCCFFCC00), Color(0x88FF6600), Color(0x44FF4500), Color.Transparent),
                center = Offset(cx, cy), radius = w * 0.08f
            ),
            radius = w * 0.08f, center = Offset(cx, cy)
        )

        // Tail flames
        for (i in 0..5) {
            val tailY = cy + 20f + i * 25f
            val sway = sin((time + i * 60f).toDouble() * PI / 180).toFloat() * 15f
            val tailW = (30f - i * 4f) * (0.5f + batteryLevel * 0.5f)
            drawLine(
                Color(0xFFFF4500).copy(alpha = 0.6f - i * 0.08f),
                Offset(cx + sway - tailW, tailY),
                Offset(cx + sway + tailW, tailY),
                strokeWidth = 4f - i * 0.5f
            )
        }
    }
}

// ============================================================
// 10. AURORA BOREALIS (Premium)
// ============================================================
@Composable
fun AuroraAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((8000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    // Pre-generate star positions outside Canvas
    val starPositions = remember {
        val rng = Random(42)
        List(60) { Pair(rng.nextFloat(), rng.nextFloat() * 0.6f) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Night sky
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF000510), Color(0xFF001020), Color(0xFF000A15))))

        // Aurora bands
        val bandCount = (batteryLevel * 5).toInt().coerceIn(2, 5)
        for (band in 0 until bandCount) {
            val baseY = h * (0.15f + band * 0.12f)
            val amplitude = 40f + band * 15f
            val frequency = 0.005f + band * 0.002f

            val path = Path()
            val pathBottom = Path()
            for (x in 0..w.toInt() step 3) {
                val xf = x.toFloat()
                val wave1 = sin((xf * frequency * 360 + time + band * 60f).toDouble() * PI / 180).toFloat()
                val wave2 = sin((xf * frequency * 0.5f * 360 + time * 0.7f + band * 90f).toDouble() * PI / 180).toFloat()
                val y = baseY + (wave1 + wave2 * 0.5f) * amplitude

                if (x == 0) {
                    path.moveTo(xf, y)
                    pathBottom.moveTo(xf, y + 60f + batteryLevel * 40f)
                } else {
                    path.lineTo(xf, y)
                    pathBottom.lineTo(xf, y + 60f + batteryLevel * 40f)
                }
            }

            // Build curtain shape
            val curtain = Path()
            curtain.addPath(path)
            val bottomPoints = mutableListOf<Offset>()
            for (x in 0..w.toInt() step 3) {
                val xf = x.toFloat()
                val wave1 = sin((xf * frequency * 360 + time + band * 60f).toDouble() * PI / 180).toFloat()
                val wave2 = sin((xf * frequency * 0.5f * 360 + time * 0.7f + band * 90f).toDouble() * PI / 180).toFloat()
                val y = baseY + (wave1 + wave2 * 0.5f) * amplitude + 60f + batteryLevel * 40f
                bottomPoints.add(Offset(xf, y))
            }
            bottomPoints.reversed().forEach { curtain.lineTo(it.x, it.y) }
            curtain.close()

            val color1 = when (band % 3) {
                0 -> Color(0xFF00FF7F)
                1 -> Color(0xFF8A2BE2)
                else -> Color(0xFF00CED1)
            }
            val shimmer = sin((time + band * 45f).toDouble() * PI / 180).toFloat() * 0.15f + 0.25f
            drawPath(curtain, color1.copy(alpha = shimmer))
        }

        // Stars
        starPositions.forEachIndexed { idx, (sx, sy) ->
            val twinkle = sin((time * 3 + idx * 30f).toDouble() * PI / 180).toFloat() * 0.3f + 0.5f
            drawCircle(Color.White.copy(alpha = twinkle * 0.8f), radius = 1.5f, center = Offset(sx * w, sy * h))
        }
    }
}

// ============================================================
// 11. PIXEL RAIN (Premium)
// ============================================================
@Composable
fun PixelRainAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pixel")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((3000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    data class PixelDrop(val column: Int, val speed: Float, val length: Int, val offset: Float)
    val columns = 40
    val drops = remember {
        List(columns) {
            PixelDrop(
                column = it,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                length = Random.nextInt(5, 20),
                offset = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cellW = w / columns
        val cellH = cellW * 1.2f
        val rows = (h / cellH).toInt() + 1

        drawRect(Color(0xFF000800))

        val activeDrops = (batteryLevel * columns).toInt().coerceIn(10, columns)
        drops.take(activeDrops).forEach { drop ->
            val t = (time * drop.speed + drop.offset) % 1f
            val headRow = (t * (rows + drop.length)).toInt()

            for (i in 0 until drop.length) {
                val row = headRow - i
                if (row < 0 || row >= rows) continue

                val alpha = if (i == 0) 1f else (1f - i.toFloat() / drop.length) * 0.8f
                val green = if (i == 0) Color(0xFFFFFFFF) else Color(0xFF00FF00)

                val x = drop.column * cellW
                val y = row * cellH
                drawRect(green.copy(alpha = alpha * batteryLevel.coerceAtLeast(0.3f)),
                    topLeft = Offset(x, y),
                    size = Size(cellW * 0.8f, cellH * 0.8f))
            }
        }
    }
}

// ============================================================
// 12. SPIRIT ENERGY / DBZ AURA (Premium)
// ============================================================
@Composable
fun SpiritEnergyAnimation(batteryLevel: Float, speed: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "spirit")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween((800 / speed).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween((2000 / speed).toInt(), easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h * 0.55f

        drawRect(Color(0xFF050005))

        // Aura rings expanding outward
        val ringCount = (batteryLevel * 6).toInt().coerceIn(2, 6)
        for (i in 0 until ringCount) {
            val ringPhase = (time / 360f + i * 0.15f) % 1f
            val radius = 30f + ringPhase * w * 0.4f
            val alpha = (1f - ringPhase) * 0.4f * pulse
            val color = if (i % 2 == 0) Color(0xFFFFD700) else Color(0xFFFF4500)

            drawCircle(color.copy(alpha = alpha), radius = radius, center = Offset(cx, cy),
                style = Stroke(width = 3f + (1f - ringPhase) * 5f))
        }

        // Central power aura
        val auraSize = w * 0.15f * (0.8f + pulse * 0.4f) * (0.5f + batteryLevel * 0.5f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xCCFFD700), Color(0x88FF6600), Color(0x44FF4500), Color.Transparent),
                center = Offset(cx, cy), radius = auraSize
            ),
            radius = auraSize, center = Offset(cx, cy)
        )

        // Rising energy wisps
        for (i in 0..12) {
            val angle = (i * 30f + time).toDouble() * PI / 180
            val dist = 20f + batteryLevel * 80f
            val wispX = cx + cos(angle).toFloat() * dist
            val wispY = cy + sin(angle).toFloat() * dist * 0.5f

            // Wisp rising upward
            val riseOffset = ((time / 360f + i * 0.08f) % 1f) * h * 0.3f
            val wispAlpha = (1f - riseOffset / (h * 0.3f)) * 0.6f * pulse

            drawCircle(Color(0xFFFFD700).copy(alpha = wispAlpha),
                radius = 5f + batteryLevel * 5f,
                center = Offset(wispX.toFloat(), wispY.toFloat() - riseOffset))
        }

        // Ground cracks (energy lines radiating from center)
        for (i in 0..7) {
            val angle = (i * 45f + time * 0.5f).toDouble() * PI / 180
            val len = 40f + batteryLevel * 100f * pulse
            val ex = cx + cos(angle).toFloat() * len
            val ey = cy + h * 0.2f + sin(angle).toFloat() * len * 0.3f

            drawLine(
                Color(0xFFFF4500).copy(alpha = 0.4f * pulse),
                Offset(cx, cy + h * 0.2f),
                Offset(ex.toFloat(), ey.toFloat()),
                strokeWidth = 2f
            )
        }
    }
}
