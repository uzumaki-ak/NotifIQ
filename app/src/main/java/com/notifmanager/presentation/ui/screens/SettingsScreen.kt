package com.notifmanager.presentation.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifmanager.presentation.viewmodel.SettingsViewModel

/**
 * SETTINGS SCREEN - App configuration and app management
 *
 * Shows:
 * - Permission status
 * - App behavior list
 * - Spammy apps
 * - App-specific settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLLMSettings: () -> Unit = {},  // ADD THIS
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allApps by viewModel.allAppBehaviors.collectAsState()
    val spammyApps by viewModel.spammyApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission card
            item {
                PermissionCard(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }




            // LLM Settings button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToLLMSettings
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🤖 AI Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure AI providers for better understanding",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }

            // Spammy apps section
            if (spammyApps.isNotEmpty()) {
                item {
                    Text(
                        text = "Spammy Apps (${spammyApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(spammyApps.take(5)) { app ->
                    AppBehaviorCard(
                        packageName = app.packageName,
                        totalReceived = app.totalReceived,
                        openRate = app.openRate,
                        isLocked = app.isLocked,
                        lockedCategory = app.lockedCategory,
                        notificationsPerHour = app.notificationsLastHour,
                        onLockToggle = {
                            if (app.isLocked) {
                                viewModel.unlockApp(app.packageName)
                            } else {
                                viewModel.lockAppToCategory(app.packageName, "SILENT")
                            }
                        },
                        onReset = {
                            viewModel.resetAppBehavior(app.packageName)
                        }
                    )
                }
            }

            // All apps section
            item {
                Text(
                    text = "All Apps (${allApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(allApps) { app ->
                AppBehaviorCard(
                    packageName = app.packageName,
                    totalReceived = app.totalReceived,
                    openRate = app.openRate,
                    isLocked = app.isLocked,
                    lockedCategory = app.lockedCategory,
                    notificationsPerHour = app.notificationsLastHour,
                    onLockToggle = {
                        if (app.isLocked) {
                            viewModel.unlockApp(app.packageName)
                        } else {
                            // Show dialog to select category
                            viewModel.lockAppToCategory(app.packageName, "NORMAL")
                        }
                    },
                    onReset = {
                        viewModel.resetAppBehavior(app.packageName)
                    }
                )
            }
        }
    }
}

/**
 * Permission status card
 */
@Composable
fun PermissionCard(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This app needs notification access to work properly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        }
    }
}

/**
 * App behavior card
 */
@Composable
fun AppBehaviorCard(
    packageName: String,
    totalReceived: Int,
    openRate: Float,
    isLocked: Boolean,
    lockedCategory: String?,
    notificationsPerHour: Int,
    onLockToggle: () -> Unit,
    onReset: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packageName.split(".").lastOrNull() ?: packageName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$totalReceived notifications • ${(openRate * 100).toInt()}% opened",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (notificationsPerHour > 5) {
                        Text(
                            text = "⚠️ $notificationsPerHour/hour (spammy)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = onLockToggle) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLocked) "Locked" else "Unlocked",
                        tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLocked && lockedCategory != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔒 Locked to $lockedCategory",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
