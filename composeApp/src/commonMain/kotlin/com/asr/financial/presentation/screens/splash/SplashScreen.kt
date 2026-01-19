package com.asr.financial.presentation.screens.splash

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.components.TwoLevelHouseIcon
import com.asr.financial.presentation.ui.constants.AppConstants
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val iconScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = AppConstants.Time.SPLASH_ANIMATION_DURATION_MS,
            delayMillis = AppConstants.Time.SPLASH_ANIMATION_DELAY_MS
        )
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(AppConstants.Time.SPLASH_SCREEN_MIN_DURATION_MS)
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            TwoLevelHouseIcon(
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale),
                color = MaterialTheme.colorScheme.onPrimary,
                // Splash screen: Light mode = black windows/door, Dark mode = white windows/door
                windowColor = if (isSystemInDarkTheme()) {
                    Color.White
                } else {
                    Color.Black
                }
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = AppConstants.Branding.APP_NAME,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = AppConstants.Branding.APP_SUBTITLE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = AppConstants.UI.DEFAULT_ALPHA),
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = AppConstants.Branding.APP_LOCATION,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = AppConstants.UI.ALPHA_HIGH),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
