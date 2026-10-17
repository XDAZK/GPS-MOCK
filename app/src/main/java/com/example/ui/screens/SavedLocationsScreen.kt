package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SavedLocation
import com.example.ui.components.SaveLocationDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.GpsMockViewModel
import java.util.Locale

@Composable
fun SavedLocationsScreen(
    viewModel: GpsMockViewModel,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val mockState by viewModel.mockEngineState.collectAsStateWithLifecycle()
    var filterText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Favorites", "Vietnam", "Global", "Gaming", "Work", "Custom")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("saved_locations_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyberCyan,
                contentColor = Color(0xFF0F172A),
                shape = CircleShape,
                modifier = Modifier.testTag("add_saved_location_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
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
                        text = "Vị trí đã lưu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${locations.size} địa điểm có sẵn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick save current location button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showAddDialog = true },
                    color = CyberEmerald.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lưu điểm hiện tại",
                            color = CyberEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search filter
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text("Tìm trong danh sách đã lưu...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                            selectedLabelColor = CyberCyan,
                            containerColor = DarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Locations List
            val displayedLocations = locations.filter {
                filterText.isBlank() || it.name.contains(filterText, ignoreCase = true) || it.description.contains(filterText, ignoreCase = true)
            }

            if (displayedLocations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có vị trí nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Nhấn nút + để thêm tọa độ yêu thích",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedLocations, key = { it.id }) { loc ->
                        SavedLocationItemCard(
                            location = loc,
                            onTeleport = {
                                viewModel.teleportTo(loc.latitude, loc.longitude, loc.altitude, loc.name)
                                onNavigateToMap()
                            },
                            onDelete = { viewModel.deleteSavedLocation(loc) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SaveLocationDialog(
            latitude = mockState.latitude,
            longitude = mockState.longitude,
            altitude = mockState.altitude,
            onSave = { name, desc, category ->
                viewModel.saveLocation(name, desc, mockState.latitude, mockState.longitude, mockState.altitude, category)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun SavedLocationItemCard(
    location: SavedLocation,
    onTeleport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_location_item_${location.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (location.category) {
                            "Vietnam" -> CyberEmerald.copy(alpha = 0.2f)
                            "Global" -> CyberPurple.copy(alpha = 0.2f)
                            else -> CyberCyan.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = when (location.category) {
                        "Vietnam" -> CyberEmerald
                        "Global" -> CyberPurple
                        else -> CyberCyan
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = location.category,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (location.description.isNotBlank()) {
                    Text(
                        text = location.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.5f, %.5f", location.latitude, location.longitude),
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Teleport Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTeleport() },
                color = CyberCyan
            ) {
                Text(
                    text = "Dịch chuyển",
                    color = Color(0xFF0F172A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
