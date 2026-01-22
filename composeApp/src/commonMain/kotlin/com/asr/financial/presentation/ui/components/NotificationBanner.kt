package com.asr.financial.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.asr.financial.domain.models.PendingNotification
import com.asr.financial.platform.UrlLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Banner component for displaying pending notifications on Home screen.
 * Shows notification title and body, with clickable HTTP/HTTPS links.
 * Includes a dismiss button (X) to clear the notification.
 */
@Composable
fun NotificationBanner(
    notification: PendingNotification,
    onDismiss: () -> Unit,
    urlLauncher: UrlLauncher,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Body text (no links in body, links come from notification.url attribute)
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Display clickable link if URL is provided
                notification.url?.takeIf { it.isNotBlank() }?.let { url ->
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            scope.launch {
                                urlLauncher.openUrl(url)
                            }
                        }
                    )
                }
            }
            
            // Dismiss button
            IconButton(
                onClick = { onDismiss() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss notification",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

