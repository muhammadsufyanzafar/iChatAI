package com.zafar.ichatai.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

enum class HelpCategory(val title: String) {
    GETTING_STARTED("Getting Started"),
    ACCOUNT_BILLING("Account & Billing"),
    USING_AI("Using the AI"),
    TROUBLESHOOTING("Troubleshooting");

    val icon: ImageVector
        get() = when (this) {
            GETTING_STARTED -> Icons.Rounded.RocketLaunch
            ACCOUNT_BILLING -> Icons.Rounded.CreditCard
            USING_AI -> Icons.Rounded.ChatBubbleOutline
            TROUBLESHOOTING -> Icons.Rounded.Build
        }
}

data class FaqData(
    val question: String,
    val answer: String,
    val category: HelpCategory
)
