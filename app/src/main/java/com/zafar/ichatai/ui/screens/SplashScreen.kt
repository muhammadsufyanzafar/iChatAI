package com.zafar.ichatai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToMain: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        onNavigateToMain()
    }

    // Modern colors based on theme
    val textColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

    GlowBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Icon with Floating Animation
                Image(
                    painter = painterResource(id = R.drawable.ichatai_icon),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .offset(y = floatAnim.dp)
                        .scale(scaleAnim.value)
                        .alpha(alphaAnim.value)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // App Name
                Text(
                    text = "iChatAI",
                    color = textColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.alpha(alphaAnim.value)
                )

                Text(
                    text = "Your Friendly AI Companion",
                    color = subTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alpha(alphaAnim.value)
                )

                Spacer(modifier = Modifier.height(80.dp))

                // Animated Pulse Effect
                PulseAnimation(isDarkTheme)
            }

            // Footer
            Text(
                text = "Muhammad Sufyan Zafar",
                color = subTextColor.copy(alpha = 0.4f),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .alpha(alphaAnim.value)
            )
        }
    }
}

@Composable
fun PulseAnimation(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryPulseColor = MaterialTheme.colorScheme.primary
    val secondaryPulseColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size.center
            val baseRadius = 30.dp.toPx()
            
            // Draw rings
            for (i in 1..3) {
                drawCircle(
                    color = primaryPulseColor.copy(alpha = pulseAlpha / (i * 1.5f)),
                    center = center,
                    radius = baseRadius + (i * 12.dp.toPx()),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
            
            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryPulseColor.copy(alpha = 0.2f), Color.Transparent),
                    center = center,
                    radius = baseRadius + 45.dp.toPx()
                ),
                center = center,
                radius = baseRadius + 45.dp.toPx()
            )
        }
    }
}
