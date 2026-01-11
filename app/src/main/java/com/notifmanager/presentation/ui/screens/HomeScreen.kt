package com.notifmanager.presentation.ui.screens
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifmanager.presentation.ui.components.CategorySection
import com.notifmanager.presentation.viewmodel.HomeViewModel
import com.notifmanager.utils.Constants

/**
 * HOME SCREEN - Main notification inbox
 *
 * Shows all notifications grouped by priority category
 * This is the main screen users see
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val groupedNotifications by viewModel.groupedNotifications.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    // Settings icon
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Show summary at top
            item {
                SummaryCard(categoryCounts)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Critical section
            val criticalNotifications = groupedNotifications[Constants.NotificationCategory.CRITICAL] ?: emptyList()
            if (criticalNotifications.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.CRITICAL,
                        notifications = criticalNotifications,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) }
                    )
                }
            }

            // Important section
            val importantNotifications = groupedNotifications[Constants.NotificationCategory.IMPORTANT] ?: emptyList()
            if (importantNotifications.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.IMPORTANT,
                        notifications = importantNotifications,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) }
                    )
                }
            }

            // Normal section
            val normalNotifications = groupedNotifications[Constants.NotificationCategory.NORMAL] ?: emptyList()
            if (normalNotifications.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.NORMAL,
                        notifications = normalNotifications,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) }
                    )
                }
            }

            // Silent section
            val silentNotifications = groupedNotifications[Constants.NotificationCategory.SILENT] ?: emptyList()
            if (silentNotifications.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.SILENT,
                        notifications = silentNotifications,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) }
                    )
                }
            }

            // Empty state
            if (groupedNotifications.values.all { it.isEmpty() }) {
                item {
                    EmptyState()
                }
            }
        }
    }
}

/**
 * Summary card showing notification counts
 */
@Composable
fun SummaryCard(categoryCounts: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem("Critical", categoryCounts["CRITICAL"] ?: 0, Color(0xFFEF4444))
                SummaryItem("Important", categoryCounts["IMPORTANT"] ?: 0, Color(0xFFF97316))
                SummaryItem("Normal", categoryCounts["NORMAL"] ?: 0, Color(0xFF3B82F6))
                SummaryItem("Silent", categoryCounts["SILENT"] ?: 0, Color(0xFF6B7280))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Empty state when no notifications
 */
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                text = "No notifications",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You're all caught up!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}