package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.R
import com.zafar.ichatai.ui.components.GlassCard
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.viewmodel.LanguageViewModel

data class LanguageOption(val nameRes: Int, val code: String, val flag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onBackClick: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isTranslateEnabled by viewModel.isTranslateEnabled.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val languages = listOf(
        LanguageOption(R.string.lang_en, "en", "🇺🇸"),
        LanguageOption(R.string.lang_es, "es", "🇪🇸"),
        LanguageOption(R.string.lang_fr, "fr", "🇫🇷"),
        LanguageOption(R.string.lang_de, "de", "🇩🇪"),
        LanguageOption(R.string.lang_zh, "zh", "🇨🇳"),
        LanguageOption(R.string.lang_it, "it", "🇮🇹"),
        LanguageOption(R.string.lang_ur, "ur", "🇵🇰"),
        LanguageOption(R.string.lang_hi, "hi", "🇮🇳"),
        LanguageOption(R.string.lang_ar, "ar", "🇸🇦"),
        LanguageOption(R.string.lang_pt, "pt", "🇵🇹")
    )

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.language_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
                ) {
                    item {
                        GlassCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.translate_incoming_messages),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.translate_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Switch(
                                    checked = isTranslateEnabled,
                                    onCheckedChange = { viewModel.setTranslateEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colorScheme.primary,
                                        checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                    item {
                        GlassCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.app_language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                languages.forEach { language ->
                                    LanguageItem(
                                        language = language,
                                        isSelected = selectedLanguage == language.code,
                                        onClick = { viewModel.setLanguage(language.code) }
                                    )
                                    if (language != languages.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = colorScheme.onSurface.copy(alpha = 0.08f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.reset_to_defaults),
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = language.flag, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(language.nameRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = colorScheme.onSurface
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colorScheme.primary
            )
        )
    }
}
