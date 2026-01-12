package com.notifmanager.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * ONBOARDING SCREEN
 *
 * UPDATED: Uses callback for permission request
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestPermission: () -> Unit = {}  // NEW
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to\nIntelligent Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureItem(
                    emoji = "🎯",
                    title = "Smart Prioritization",
                    description = "Automatically sorts notifications by importance"
                )

                FeatureItem(
                    emoji = "📱",
                    title = "Channel-Level Learning",
                    description = "Learns which YouTube channels & contacts you care about"
                )

                FeatureItem(
                    emoji = "🧠",
                    title = "Gets Smarter Over Time",
                    description = "Adapts to your behavior automatically"
                )

                FeatureItem(
                    emoji = "🔇",
                    title = "Silence Spam",
                    description = "Automatically detects and quiets noisy apps"
                )

                FeatureItem(
                    emoji = "🔒",
                    title = "100% Private",
                    description = "All processing happens locally on your device"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "To get started, you need to grant notification access.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,  // UPDATED
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I'll do this later")
        }
    }
}

@Composable
fun FeatureItem(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}