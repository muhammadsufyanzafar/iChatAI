package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToQuietHours: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.notifications),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    NotificationSection(title = stringResource(R.string.general_notifications)) {
                        NotificationToggleItem(
                            title = stringResource(R.string.allow_notifications),
                            checked = prefs.allowNotifications,
                            onCheckedChange = { viewModel.toggleAllowNotifications(it) }
                        )
                    }
                }

                item {
                    NotificationSection(title = stringResource(R.string.notification_types)) {
                        NotificationToggleItem(
                            title = stringResource(R.string.push_notifications),
                            icon = Icons.Outlined.NotificationsActive,
                            checked = prefs.pushNotifications,
                            onCheckedChange = { viewModel.togglePushNotifications(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        NotificationToggleItem(
                            title = stringResource(R.string.sound_vibration),
                            icon = Icons.Outlined.VolumeUp,
                            checked = prefs.soundAndVibration,
                            onCheckedChange = { viewModel.toggleSoundAndVibration(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        NotificationToggleItem(
                            title = stringResource(R.string.vibration),
                            icon = Icons.Default.Vibration,
                            checked = prefs.vibrationEnabled,
                            onCheckedChange = { viewModel.toggleVibration(it) }
                        )
                    }
                }

                item {
                    NotificationSection(title = stringResource(R.string.specific_alerts)) {
                        NotificationToggleItem(
                            title = stringResource(R.string.new_feature_announcements),
                            checked = prefs.newFeatureAnnouncements,
                            onCheckedChange = { viewModel.toggleNewFeatureAnnouncements(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        NotificationToggleItem(
                            title = stringResource(R.string.daily_streak_reminder),
                            checked = prefs.dailyStreakReminder,
                            onCheckedChange = { viewModel.toggleDailyStreakReminder(it) }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = onNavigateToQuietHours) {
                            Text(
                                stringResource(R.string.quiet_hours_config),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
        )
        GlassCard {
            content()
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f)
            )
        )
    }
}
