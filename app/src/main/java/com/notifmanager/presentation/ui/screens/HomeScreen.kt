package com.notifmanager.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifmanager.presentation.ui.components.CategorySection
import com.notifmanager.presentation.viewmodel.HomeViewModel
import com.notifmanager.utils.Constants

/**
 * HOME SCREEN - COMPLETELY FIXED UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPreferences: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val groupedNotifications by viewModel.groupedNotifications.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // FIXED: Proper title with no overlap
//                    Text("Notifications")
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }

                    // FIXED: Settings button with proper spacing
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }

                    // Dropdown for time filter
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Last 2 hours") },
                            onClick = {
                                viewModel.setTimeFilter(TimeFilter.LAST_2_HOURS)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last 5 hours") },
                            onClick = {
                                viewModel.setTimeFilter(TimeFilter.LAST_5_HOURS)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Today") },
                            onClick = {
                                viewModel.setTimeFilter(TimeFilter.TODAY)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last 2 days") },
                            onClick = {
                                viewModel.setTimeFilter(TimeFilter.LAST_2_DAYS)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All time") },
                            onClick = {
                                viewModel.setTimeFilter(TimeFilter.ALL_TIME)
                                showFilterMenu = false
                            }
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
            contentPadding = PaddingValues(16.dp)
        ) {
            // Current filter indicator
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Showing: ${timeFilter.displayName}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary card
            item {
                SummaryCard(categoryCounts)
                Spacer(modifier = Modifier.height(16.dp))
            }


            // CLEAR ALL button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.clearAllNotifications() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🗑️ Clear All Notifications")
                    }

                    Button(
                        onClick = onNavigateToPreferences,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚙️ Preferences")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            // Button to manage channel preferences
            item {
                Button(
                    onClick = onNavigateToPreferences,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(" Manage Channel Preferences")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Critical notifications
            val critical = groupedNotifications[Constants.NotificationCategory.CRITICAL] ?: emptyList()
            if (critical.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.CRITICAL,
                        notifications = critical,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) },
                        onMarkImportant = { viewModel.markChannelImportant(it) },  // NEW
                        onMarkSilent = { viewModel.markChannelSilent(it) }          // NEW
                    )
                }
            }

            // Important notifications
            val important = groupedNotifications[Constants.NotificationCategory.IMPORTANT] ?: emptyList()
            if (important.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.IMPORTANT,
                        notifications = important,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) },
                        onMarkImportant = { viewModel.markChannelImportant(it) },
                        onMarkSilent = { viewModel.markChannelSilent(it) }
                    )
                }
            }

            // Normal notifications
            val normal = groupedNotifications[Constants.NotificationCategory.NORMAL] ?: emptyList()
            if (normal.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.NORMAL,
                        notifications = normal,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) },
                        onMarkImportant = { viewModel.markChannelImportant(it) },
                        onMarkSilent = { viewModel.markChannelSilent(it) }
                    )
                }
            }

            // Silent notifications
            val silent = groupedNotifications[Constants.NotificationCategory.SILENT] ?: emptyList()
            if (silent.isNotEmpty()) {
                item {
                    CategorySection(
                        category = Constants.NotificationCategory.SILENT,
                        notifications = silent,
                        onNotificationDismiss = { viewModel.dismissNotification(it) },
                        onNotificationClick = { viewModel.markAsOpened(it) },
                        onMarkImportant = { viewModel.markChannelImportant(it) },
                        onMarkSilent = { viewModel.markChannelSilent(it) }
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

@Composable
fun SummaryCard(categoryCounts: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No notifications", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("You're all caught up!", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

enum class TimeFilter(val displayName: String, val hoursAgo: Int?) {
    LAST_2_HOURS("Last 2 hours", 2),
    LAST_5_HOURS("Last 5 hours", 5),
    TODAY("Today", 24),
    LAST_2_DAYS("Last 2 days", 48),
    ALL_TIME("All time", null)
}