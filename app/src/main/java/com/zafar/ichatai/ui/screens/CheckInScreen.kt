package com.zafar.ichatai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.zafar.ichatai.viewmodel.CheckInViewModel
import com.zafar.ichatai.ui.components.GlowBackground
import com.zafar.ichatai.ui.components.GlassCard
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBackClick: () -> Unit = {},
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val checkInState by viewModel.checkInState.collectAsState()
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    val handleCheckIn = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        viewModel.performCheckIn()
    }

    val canCheckInToday = remember(checkInState.lastCheckInMillis) {
        val lastCal = Calendar.getInstance().apply { timeInMillis = checkInState.lastCheckInMillis }
        val nowCal = Calendar.getInstance()
        checkInState.lastCheckInMillis == 0L || 
        lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR) ||
        lastCal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR)
    }

    val streakActive = checkInState.currentStreak > 0
    val fireColor by animateColorAsState(
        targetValue = if (streakActive) Color(0xFFFF5722) else Color(0xFF94A3B8),
        label = "fireColor"
    )

    GlowBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daily Check-in", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Streak Card
                item(span = { GridItemSpan(3) }) {
                    StreakInfoCard(checkInState.currentStreak, fireColor, streakActive)
                }

                // Days 1 to 6
                items(6) { index ->
                    val dayNum = index + 1
                    val isClaimed = index < checkInState.currentStreak
                    val isCurrent = index == checkInState.currentStreak && canCheckInToday
                    
                    DayCard(
                        day = dayNum,
                        reward = 5 + (index * 5),
                        isClaimed = isClaimed,
                        isCurrent = isCurrent,
                        onCheckIn = handleCheckIn
                    )
                }

                // Day 7 Jackpot Card
                item(span = { GridItemSpan(3) }) {
                    val isDay7Claimed = checkInState.currentStreak == 7 && !canCheckInToday
                    val isDay7Current = checkInState.currentStreak == 6 && canCheckInToday
                    
                    JackpotCard(
                        isClaimed = isDay7Claimed,
                        isCurrent = isDay7Current,
                        onCheckIn = handleCheckIn
                    )
                }
            }
        }
    }
}

@Composable
fun StreakInfoCard(streak: Int, fireColor: Color, streakActive: Boolean) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        if (streakActive) 20.dp else 0.dp,
                        CircleShape,
                        ambientColor = fireColor,
                        spotColor = fireColor
                    )
                    .clip(CircleShape)
                    .background(fireColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Whatshot,
                    contentDescription = null,
                    tint = fireColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your 7-Day Check-in Streak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { streak / 7f },
                        modifier = Modifier.size(80.dp),
                        color = Color(0xFF8B5CF6),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        strokeWidth = 8.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Text(
                        text = "$streak/7",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = "Check in daily to claim rewards and win a jackpot on Day 7!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DayCard(
    day: Int,
    reward: Int,
    isClaimed: Boolean,
    isCurrent: Boolean,
    onCheckIn: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        alpha = if (isClaimed) 0.2f else 0.4f,
        borderAlpha = if (isCurrent) 0.6f else 0.1f,
        borderWidth = if (isCurrent) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Day $day",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if (isClaimed) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                }
            }

            Text(
                text = "+$reward",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface
            )

            if (isCurrent) {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Check In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = if (isClaimed) "Claimed" else "Locked",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun JackpotCard(
    isClaimed: Boolean,
    isCurrent: Boolean,
    onCheckIn: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        borderAlpha = if (isCurrent) 0.8f else 0.1f,
        borderWidth = if (isCurrent) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Day 7",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "JACKPOT!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else Color.White
                )
                Text(
                    text = "+100 Credits",
                    fontSize = 14.sp,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else Color(0xFFFFD700)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = if (isClaimed) Color.Gray else Color(0xFFBB86FC),
                    modifier = Modifier.size(48.dp)
                )
                
                if (isCurrent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onCheckIn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("Claim", fontWeight = FontWeight.Bold)
                    }
                } else if (isClaimed) {
                    Text("Collected", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
