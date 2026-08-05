package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.theme.AccentColor
import com.zafar.ichatai.ui.theme.ThemeMode
import com.zafar.ichatai.viewmodel.AppearanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: AppearanceViewModel,
    onBackClick: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.appearance),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = colorScheme.onBackground
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
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
            ) {
                // Theme Mode Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.theme_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground.copy(alpha = 0.9f)
                        )
                        
                        GlassCard {
                            Column(modifier = Modifier.padding(8.dp)) {
                                ThemeOptionItem(
                                    title = stringResource(R.string.light),
                                    selected = themeMode == ThemeMode.LIGHT,
                                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                ThemeOptionItem(
                                    title = stringResource(R.string.dark),
                                    selected = themeMode == ThemeMode.DARK,
                                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                ThemeOptionItem(
                                    title = stringResource(R.string.system_default),
                                    selected = themeMode == ThemeMode.SYSTEM,
                                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                                )
                            }
                        }
                    }
                }

                // Accent Color Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = stringResource(R.string.accent_color),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground.copy(alpha = 0.9f)
                        )
                        
                        GlassCard {
                            Box(modifier = Modifier.padding(20.dp)) {
                                AccentColorGrid(
                                    selectedColor = accentColor,
                                    onColorSelected = { viewModel.setAccentColor(it) }
                                )
                            }
                        }
                    }
                }

                // Reset Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = stringResource(R.string.reset_to_defaults),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = null, // Handled by row clickable
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun AccentColorGrid(
    selectedColor: AccentColor,
    onColorSelected: (AccentColor) -> Unit
) {
    val colors = AccentColor.entries
    
    // Using a manual FlowRow-like layout since LazyVerticalGrid might be overkill or tricky inside item
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.take(3).forEach { color ->
                AccentColorItem(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.drop(3).forEach { color ->
                AccentColorItem(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun AccentColorItem(
    color: AccentColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color.color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(color.displayNameRes),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
