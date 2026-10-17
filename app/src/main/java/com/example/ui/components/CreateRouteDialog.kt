package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedLocation
import com.example.data.model.Waypoint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import java.util.Locale

@Composable
fun CreateRouteDialog(
    currentLat: Double,
    currentLng: Double,
    savedLocations: List<SavedLocation>,
    onSave: (
        name: String,
        description: String,
        speedKmh: Float,
        intervalSeconds: Int,
        loopMode: String,
        scheduleType: String,
        waypoints: List<Waypoint>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var speedKmh by remember { mutableFloatStateOf(15f) }
    var intervalSeconds by remember { mutableIntStateOf(2) }
    var loopMode by remember { mutableStateOf("LOOP") } // LOOP, PING_PONG, ONCE
    var scheduleType by remember { mutableStateOf("CONTINUOUS") } // CONTINUOUS or TIMED_HOP

    val waypoints = remember {
        mutableStateListOf(
            Waypoint(currentLat, currentLng, name = "Điểm khởi hành", waitSeconds = 10)
        )
    }

    var showAddWaypointSheet by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AltRoute,
                contentDescription = "Route Schedule",
                tint = CyberCyan
            )
        },
        title = {
            Text(
                text = "Lập lịch trình & Lộ trình mới",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên lịch trình / lộ trình") },
                        placeholder = { Text("vd: Đi dạo Hồ Gươm, Lộ trình giao hàng...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Loại di chuyển:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = scheduleType == "CONTINUOUS",
                            onClick = { scheduleType = "CONTINUOUS" },
                            label = { Text("Di chuyển liên tục") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                        FilterChip(
                            selected = scheduleType == "TIMED_HOP",
                            onClick = { scheduleType = "TIMED_HOP" },
                            label = { Text("Nhảy theo giờ") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberEmerald.copy(alpha = 0.25f),
                                selectedLabelColor = CyberEmerald
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = speedKmh.toInt().toString(),
                            onValueChange = { speedKmh = it.toFloatOrNull() ?: 15f },
                            label = { Text("Tốc độ (km/h)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("route_speed_input")
                        )
                        OutlinedTextField(
                            value = intervalSeconds.toString(),
                            onValueChange = { intervalSeconds = it.toIntOrNull() ?: 2 },
                            label = { Text("Chu kỳ (giây)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("route_interval_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Chế độ lặp lại:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("LOOP" to "Vòng tròn", "PING_PONG" to "Khứ hồi", "ONCE" to "1 lần").forEach { (code, label) ->
                            FilterChip(
                                selected = loopMode == code,
                                onClick = { loopMode = code },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Các điểm dừng (${waypoints.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showAddWaypointSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.25f)),
                            modifier = Modifier.testTag("add_waypoint_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm điểm", color = CyberCyan, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(waypoints) { idx, wp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wp.name.ifEmpty { "Điểm ${idx + 1}" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.5f, %.5f • Chờ: %ds", wp.latitude, wp.longitude, wp.waitSeconds),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (waypoints.size > 1) {
                                IconButton(
                                    onClick = { waypoints.removeAt(idx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && waypoints.isNotEmpty()) {
                        onSave(
                            name.trim(),
                            description.trim(),
                            speedKmh,
                            intervalSeconds,
                            loopMode,
                            scheduleType,
                            waypoints.toList()
                        )
                    }
                },
                enabled = name.isNotBlank() && waypoints.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("confirm_create_route_button")
            ) {
                Text("Lưu Lịch Trình", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )

    if (showAddWaypointSheet) {
        AddWaypointDialog(
            currentLat = currentLat,
            currentLng = currentLng,
            savedLocations = savedLocations,
            onAdd = { newWp ->
                waypoints.add(newWp)
                showAddWaypointSheet = false
            },
            onDismiss = { showAddWaypointSheet = false }
        )
    }
}

@Composable
fun AddWaypointDialog(
    currentLat: Double,
    currentLng: Double,
    savedLocations: List<SavedLocation>,
    onAdd: (Waypoint) -> Unit,
    onDismiss: () -> Unit
) {
    var latText by remember { mutableStateOf(String.format(Locale.US, "%.5f", currentLat)) }
    var lngText by remember { mutableStateOf(String.format(Locale.US, "%.5f", currentLng)) }
    var nameText by remember { mutableStateOf("") }
    var waitSecondsText by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm điểm vào lịch trình") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quick pick from saved locations
                if (savedLocations.isNotEmpty()) {
                    Text("Chọn nhanh từ vị trí đã lưu:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        items(savedLocations.size) { i ->
                            val loc = savedLocations[i]
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        latText = String.format(Locale.US, "%.5f", loc.latitude)
                                        lngText = String.format(Locale.US, "%.5f", loc.longitude)
                                        nameText = loc.name
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${loc.name} (${String.format(Locale.US, "%.3f, %.3f", loc.latitude, loc.longitude)})",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Tên điểm dừng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Vĩ độ") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Kinh độ") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = waitSecondsText,
                    onValueChange = { waitSecondsText = it },
                    label = { Text("Thời gian dừng tại điểm (giây)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: currentLat
                    val lng = lngText.toDoubleOrNull() ?: currentLng
                    val waitSec = waitSecondsText.toIntOrNull() ?: 10
                    onAdd(
                        Waypoint(
                            latitude = lat,
                            longitude = lng,
                            name = nameText.ifBlank { "Điểm (${String.format(Locale.US, "%.4f, %.4f", lat, lng)})" },
                            waitSeconds = waitSec
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Thêm", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
