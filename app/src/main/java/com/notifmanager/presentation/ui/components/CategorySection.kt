package com.notifmanager.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifmanager.data.database.entities.NotificationEntity
import com.notifmanager.utils.Constants

/**
 * CATEGORY SECTION - UPDATED with action callbacks
 */
@Composable
fun CategorySection(
    category: Constants.NotificationCategory,
    notifications: List<NotificationEntity>,
    onNotificationDismiss: (Long) -> Unit,
    onNotificationClick: (Long) -> Unit,
    onMarkImportant: (Long) -> Unit,  // NEW
    onMarkSilent: (Long) -> Unit,     // NEW
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val categoryColor = when (category) {
        Constants.NotificationCategory.CRITICAL -> Color(0xFFEF4444)
        Constants.NotificationCategory.IMPORTANT -> Color(0xFFF97316)
        Constants.NotificationCategory.NORMAL -> Color(0xFF3B82F6)
        Constants.NotificationCategory.SILENT -> Color(0xFF6B7280)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )

                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (notifications.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = categoryColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = notifications.size.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }

        // Notification list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifications.isEmpty()) {
                    Text(
                        text = "No notifications",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notifications.forEach { notification ->
                        NotificationCard(
                            notification = notification,
                            onDismiss = { onNotificationDismiss(notification.id) },
                            onClick = { onNotificationClick(notification.id) },
                            onMarkImportant = { onMarkImportant(notification.id) },  // NEW
                            onMarkSilent = { onMarkSilent(notification.id) }          // NEW
                        )
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}