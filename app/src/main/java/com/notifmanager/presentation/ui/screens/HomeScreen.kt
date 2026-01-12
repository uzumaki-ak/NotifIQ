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
 * HOME SCREEN - Main notification inbox
 *
 * FIXED: Settings button overlap, Notifications text position
 * ADDED: Time filter dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
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
                    Text(
                        text = "Notifications",
                        modifier = Modifier.padding(start = 0.dp) // FIX: No extra padding
                    )
                },
                actions = {
                    // Time filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter by time"
                        )
                    }

                    // Settings button (FIXED: proper spacing)
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.padding(end = 8.dp) // FIX: Add spacing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }

                    // Time filter dropdown menu
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
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Show current filter
            item {
                FilterChip(timeFilter)
                Spacer(modifier = Modifier.height(8.dp))
            }

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
 * Filter chip showing current time filter
 */
@Composable
fun FilterChip(timeFilter: TimeFilter) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "Showing: ${timeFilter.displayName}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

/**
 * Time filter enum
 */
enum class TimeFilter(val displayName: String, val hoursAgo: Int?) {
    LAST_2_HOURS("Last 2 hours", 2),
    LAST_5_HOURS("Last 5 hours", 5),
    TODAY("Today", 24),
    LAST_2_DAYS("Last 2 days", 48),
    ALL_TIME("All time", null)
}