package com.acmeai.chargevibe.ui.gallery

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acmeai.chargevibe.ads.AdManager
import com.acmeai.chargevibe.animation.RenderAnimation
import com.acmeai.chargevibe.data.AnimationInfo
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.data.animations
import kotlinx.coroutines.launch

@Composable
fun GalleryScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val selectedAnimation by prefs.selectedAnimation.collectAsState(initial = 0)
    val unlockedAnimations by prefs.unlockedAnimations.collectAsState(initial = emptySet())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Animation Gallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Select your charging animation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(animations) { index, anim ->
                // Native ad placeholder every 5th item
                val isSelected = selectedAnimation == index
                val isUnlocked = !anim.isPremium || unlockedAnimations.contains(anim.id)

                AnimationCard(
                    anim = anim,
                    index = index,
                    isSelected = isSelected,
                    isUnlocked = isUnlocked,
                    onClick = {
                        if (isUnlocked) {
                            scope.launch { prefs.setSelectedAnimation(index) }
                        } else {
                            // Show rewarded ad to unlock
                            activity?.let { act ->
                                AdManager.showRewarded(act) {
                                    scope.launch { prefs.unlockAnimation(anim.id) }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnimationCard(
    anim: AnimationInfo,
    index: Int,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !isUnlocked -> Color(0xFF444444)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animation preview (smaller)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                RenderAnimation(
                    animationIndex = index,
                    batteryLevel = 0.65f,
                    speed = 0.5f // Slow in gallery for perf
                )
            }

            // Info section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = anim.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = anim.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Status icons
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            } else if (!isUnlocked) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF8B5CF6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Watch", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
