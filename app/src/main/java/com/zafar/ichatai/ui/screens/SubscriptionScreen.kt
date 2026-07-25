package com.zafar.ichatai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.theme.DarkBaseBackground
import com.zafar.ichatai.ui.theme.DarkCardContainer
import com.zafar.ichatai.ui.theme.DarkPrimaryAccent
import com.zafar.ichatai.ui.theme.DarkSecondaryAccent
import com.zafar.ichatai.ui.theme.DarkSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    GlowBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Subscription",
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { /* Restore Purchases Logic */ }) {
                            Text(
                                text = "Restore",
                                color = colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Current Plan Card
                CurrentPlanCard()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Available Plans",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlanCard(
                        modifier = Modifier.weight(1f),
                        title = "Pro Monthly",
                        price = "$19.99",
                        duration = "month",
                        features = listOf(
                            "Unlimited Messages",
                            "Faster AI Responses",
                            "Priority Support"
                        ),
                        buttonText = "Coming Soon",
                        isPopular = false
                    )
                    PlanCard(
                        modifier = Modifier.weight(1f),
                        title = "Pro Annual",
                        price = "$159.99",
                        duration = "year",
                        features = listOf(
                            "All Pro Monthly Benefits",
                            "Access to Beta Features",
                            "Early Access to New Models"
                        ),
                        buttonText = "Coming Soon",
                        isPopular = true,
                        savingsText = "(Save 20%)"
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CurrentPlanCard() {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Current Plan: Free Plan",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Renews: N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Benefits Active",
                style = MaterialTheme.typography.bodyLarge,
                color = DarkSuccess,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Manage Billing */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Manage Billing", color = colorScheme.onSurface, fontSize = 14.sp)
                }
                Button(
                    onClick = { /* Switch Plan */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Switch Plan", color = colorScheme.onSurface, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    duration: String,
    features: List<String>,
    buttonText: String,
    isPopular: Boolean = false,
    savingsText: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPopular) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isPopular) {
                Surface(
                    color = colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Best Value",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(22.dp)) // Maintain alignment with the other card
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = " / $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            
            if (savingsText != null) {
                Text(
                    text = savingsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp).offset(y = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 16.sp,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* Not active */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = colorScheme.onSurface.copy(alpha = 0.05f),
                    disabledContentColor = colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(buttonText, fontSize = 12.sp)
            }
        }
    }
}
