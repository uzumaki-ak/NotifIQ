package com.notifmanager.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifmanager.data.models.LLMProvider
import com.notifmanager.presentation.viewmodel.LLMSettingsViewModel

/**
 * LLM SETTINGS SCREEN - Configure AI providers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LLMSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LLMSettingsViewModel = hiltViewModel()
) {
    val isEnabled by viewModel.isLLMEnabled.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Settings") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable/Disable LLM
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable AI Understanding",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Use AI to better understand notification content",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { viewModel.setLLMEnabled(it) }
                            )
                        }
                    }
                }
            }

            if (isEnabled) {
                // Provider selection
                item {
                    Text(
                        text = "Select AI Provider",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                LLMProvider.values().forEach { provider ->
                    item {
                        ProviderCard(
                            provider = provider,
                            isSelected = provider == selectedProvider,
                            apiKey = apiKeys[provider.name] ?: "",
                            onSelect = { viewModel.selectProvider(provider) },
                            onApiKeyChange = { viewModel.setApiKey(provider, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: LLMProvider,
    isSelected: Boolean,
    apiKey: String,
    onSelect: () -> Unit,
    onApiKeyChange: (String) -> Unit
) {
    var showApiKey by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                placeholder = { Text("Enter your API key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showApiKey) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle visibility"
                        )
                    }
                }
            )
        }
    }
}