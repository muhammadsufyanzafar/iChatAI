package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuietHoursScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Quiet Hours",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                NotificationSection(title = "General") {
                    NotificationToggleItem(
                        title = "Enable Quiet Hours",
                        checked = prefs.quietHoursEnabled,
                        onCheckedChange = { viewModel.toggleQuietHours(it) }
                    )
                }

                if (prefs.quietHoursEnabled) {
                    NotificationSection(title = "Schedule") {
                        TimeSelectionItem(
                            title = "Start Time",
                            time = prefs.quietHoursStart,
                            onClick = { showStartTimePicker = true }
                        )
                        TimeSelectionItem(
                            title = "End Time",
                            time = prefs.quietHoursEnd,
                            onClick = { showEndTimePicker = true }
                        )
                    }
                }

                if (showStartTimePicker || showEndTimePicker) {
                    val initialTime = if (showStartTimePicker) prefs.quietHoursStart else prefs.quietHoursEnd
                    val initialHour = initialTime.split(":")[0].toIntOrNull() ?: 0
                    val initialMinute = initialTime.split(":")[1].toIntOrNull() ?: 0
                    
                    val timePickerState = rememberTimePickerState(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                        is24Hour = true
                    )

                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { 
                            showStartTimePicker = false
                            showEndTimePicker = false
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                    if (showStartTimePicker) {
                                        viewModel.updateQuietHours(time, prefs.quietHoursEnd)
                                    } else {
                                        viewModel.updateQuietHours(prefs.quietHoursStart, time)
                                    }
                                    showStartTimePicker = false
                                    showEndTimePicker = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))
                            ) {
                                Text("OK")
                            }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelectionItem(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color(0xFFB39DDB)
            )
        }
    }
}
