package com.zafar.ichatai.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

import com.zafar.ichatai.R

enum class HelpCategory(val titleRes: Int) {
    GETTING_STARTED(R.string.help_getting_started),
    ACCOUNT_BILLING(R.string.help_account_billing),
    USING_AI(R.string.help_using_ai),
    TROUBLESHOOTING(R.string.help_troubleshooting);

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
