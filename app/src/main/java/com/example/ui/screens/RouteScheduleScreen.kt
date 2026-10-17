package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RouteSchedule
import com.example.data.model.Waypoint
import com.example.service.MockMode
import com.example.ui.components.CreateRouteDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.GpsMockViewModel
import java.util.Locale

@Composable
fun RouteScheduleScreen(
    viewModel: GpsMockViewModel,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routes by viewModel.routeSchedules.collectAsStateWithLifecycle()
    val mockState by viewModel.mockEngineState.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val showCreateDialog by viewModel.showCreateRouteDialog.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("route_schedule_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setCreateRouteDialogVisible(true) },
                containerColor = CyberCyan,
                contentColor = Color(0xFF0F172A),
                shape = CircleShape,
                modifier = Modifier.testTag("create_route_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Route")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lịch trình & Lộ trình",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tự động di chuyển GPS theo chu kỳ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${routes.size} lịch trình",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (routes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Chưa có lịch trình nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nhấn nút + bên dưới để tạo lộ trình tự động chuyển vị trí",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(routes, key = { it.id }) { route ->
                        val isCurrentRouteActive = mockState.isMocking &&
                                (mockState.mode == MockMode.ROUTE || mockState.mode == MockMode.SCHEDULED) &&
                                mockState.activeRoute?.name == route.name

                        val waypoints = viewModel.parseWaypoints(route.waypointsJson)

                        RouteScheduleItemCard(
                            route = route,
                            waypoints = waypoints,
                            isActive = isCurrentRouteActive,
                            isPaused = mockState.isPaused,
                            currentIndex = mockState.currentWaypointIndex,
                            waitSecondsRemaining = mockState.waitSecondsRemaining,
                            onStart = {
                                viewModel.startRoute(route)
                                onNavigateToMap()
                            },
                            onStop = { viewModel.stopMocking() },
                            onTogglePause = { viewModel.togglePause() },
                            onDelete = { viewModel.deleteRoute(route) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRouteDialog(
            currentLat = mockState.latitude,
            currentLng = mockState.longitude,
            savedLocations = savedLocations,
            onSave = { name, desc, speed, interval, loop, schedType, wps ->
                viewModel.createRoute(name, desc, speed, interval, loop, schedType, wps)
            },
            onDismiss = { viewModel.setCreateRouteDialogVisible(false) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteScheduleItemCard(
    route: RouteSchedule,
    waypoints: List<Waypoint>,
    isActive: Boolean,
    isPaused: Boolean,
    currentIndex: Int,
    waitSecondsRemaining: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route_schedule_card_${route.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkSurfaceVariant else DarkSurface
        ),
        elevation = CardDefaults.cardElevation(if (isActive) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name + Active indicator + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (route.scheduleType == "TIMED_HOP") Icons.Default.Schedule else Icons.Default.AltRoute,
                        contentDescription = null,
                        tint = if (isActive) CyberEmerald else CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = route.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyberEmerald.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isPaused) "TẠM DỪNG" else "ĐANG CHẠY",
                            color = if (isPaused) CyberOrange else CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Route",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (route.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceVariant) {
                    Text(
                        text = "${route.speedKmh.toInt()} km/h",
                        fontSize = 11.sp,
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceVariant) {
                    Text(
                        text = if (route.scheduleType == "TIMED_HOP") "Nhảy theo giờ" else "Di chuyển mượt",
                        fontSize = 11.sp,
                        color = CyberEmerald,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceVariant) {
                    Text(
                        text = when (route.loopMode) {
                            "LOOP" -> "Lặp vòng tròn"
                            "PING_PONG" -> "Khứ hồi"
                            else -> "Chạy 1 lần"
                        },
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(6.dp), color = DarkSurfaceVariant) {
                    Text(
                        text = "${waypoints.size} điểm dừng",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waypoints visual preview chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                waypoints.forEachIndexed { idx, wp ->
                    val isCurrentStep = isActive && idx == currentIndex
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrentStep) CyberEmerald.copy(alpha = 0.3f) else Color(0xFF0F172A),
                        border = if (isCurrentStep) androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald) else null
                    ) {
                        Text(
                            text = "${idx + 1}. ${wp.name.ifEmpty { "P${idx + 1}" }}",
                            fontSize = 10.sp,
                            color = if (isCurrentStep) CyberEmerald else Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTogglePause,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPaused) "Tiếp tục" else "Tạm dừng",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dừng lộ trình", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("start_route_button_${route.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.25f))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bắt đầu chạy lộ trình này", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
