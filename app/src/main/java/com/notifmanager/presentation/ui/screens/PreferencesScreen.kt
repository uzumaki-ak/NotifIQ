package com.notifmanager.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifmanager.presentation.viewmodel.PreferencesViewModel

/**
 * PREFERENCES SCREEN - Manage channel/sender preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val preferences by viewModel.allPreferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Channel & Sender Preferences") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (preferences.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No preferences yet.\n\nUse 'Mark Important' or 'Mark Silent' buttons on notifications to set preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(preferences) { pref ->
                    PreferenceCard(
                        preference = pref,
                        onDelete = { viewModel.deletePreference(pref) },
                        onScoreChange = { newScore ->
                            viewModel.updatePreferenceScore(pref.id, newScore)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceCard(
    preference: com.notifmanager.data.database.entities.ContentPreferenceEntity,
    onDelete: () -> Unit,
    onScoreChange: (Int) -> Unit
) {
    var sliderValue by remember { mutableStateOf(preference.preferenceScore.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preference.contentId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = preference.contentType.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score slider
            Text(
                text = "Priority: ${getPreferenceLabel(sliderValue.toInt())}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = getPreferenceColor(sliderValue.toInt())
            )

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onScoreChange(sliderValue.toInt()) },
                valueRange = -20f..20f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = getPreferenceColor(sliderValue.toInt()),
                    activeTrackColor = getPreferenceColor(sliderValue.toInt())
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Silent", style = MaterialTheme.typography.labelSmall)
                Text("Important", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

fun getPreferenceLabel(score: Int): String {
    return when {
        score >= 15 -> "Very Important"
        score >= 5 -> "Important"
        score >= -5 -> "Normal"
        score >= -15 -> "Low Priority"
        else -> "Silent"
    }
}

fun getPreferenceColor(score: Int): androidx.compose.ui.graphics.Color {
    return when {
        score >= 10 -> androidx.compose.ui.graphics.Color(0xFFF97316)
        score >= 0 -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        else -> androidx.compose.ui.graphics.Color(0xFF6B7280)
    }
}