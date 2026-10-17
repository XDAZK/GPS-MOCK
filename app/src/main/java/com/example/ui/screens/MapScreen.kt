package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.MockMode
import com.example.ui.components.CoordinateInputDialog
import com.example.ui.components.JoystickController
import com.example.ui.components.SaveLocationDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.GpsMockViewModel
import java.util.Locale

class WebAppInterface(private val onMapClick: (lat: Double, lng: Double) -> Unit) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onLocationSelected(lat: Double, lng: Double) {
        mainHandler.post {
            onMapClick(lat, lng)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(
    viewModel: GpsMockViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mockState by viewModel.mockEngineState.collectAsStateWithLifecycle()
    val showJoystick by viewModel.showJoystick.collectAsStateWithLifecycle()
    val showCoordDialog by viewModel.showCoordinateDialog.collectAsStateWithLifecycle()
    val showSaveDialog by viewModel.showSaveLocationDialog.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Update map marker whenever Kotlin mock location changes
    LaunchedEffect(mockState.latitude, mockState.longitude, isMapLoaded) {
        if (isMapLoaded && webViewRef != null) {
            val js = "javascript:updateMockMarker(${mockState.latitude}, ${mockState.longitude}, ${mockState.bearing});"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("map_screen_container")) {
        // Embedded Leaflet Map WebView
        AndroidView(
            modifier = Modifier.fillMaxSize().testTag("leaflet_map_view"),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false

                    addJavascriptInterface(
                        WebAppInterface { lat, lng ->
                            viewModel.teleportTo(lat, lng)
                        },
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                            // Initial center
                            val js = "javascript:initMap(${mockState.latitude}, ${mockState.longitude});"
                            evaluateJavascript(js, null)
                        }
                    }

                    loadDataWithBaseURL(
                        "https://openstreetmap.org",
                        generateMapHtml(mockState.latitude, mockState.longitude),
                        "text/html",
                        "UTF-8",
                        null
                    )
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            }
        )

        // Top Floating Search Bar & Results
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("map_search_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchPlaces(it) },
                        placeholder = { Text("Tìm kiếm địa chỉ, địa danh, tọa độ...", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                    )

                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearSearch() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search results drop-down list
            AnimatedVisibility(visible = searchResults.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .shadow(12.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchResults) { result ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.teleportTo(result.latitude, result.longitude, name = result.displayName)
                                        viewModel.clearSearch()
                                        val js = "javascript:centerMap(${result.latitude}, ${result.longitude});"
                                        webViewRef?.evaluateJavascript(js, null)
                                    },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditLocationAlt,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = result.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Side Quick Action Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter Button
            FloatingActionButton(
                onClick = {
                    val js = "javascript:centerMap(${mockState.latitude}, ${mockState.longitude});"
                    webViewRef?.evaluateJavascript(js, null)
                },
                modifier = Modifier.size(44.dp).testTag("recenter_map_fab"),
                containerColor = DarkSurfaceVariant,
                contentColor = CyberCyan,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on Mock Location",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Enter Precision Coordinates
            FloatingActionButton(
                onClick = { viewModel.setCoordinateDialogVisible(true) },
                modifier = Modifier.size(44.dp).testTag("enter_coordinates_fab"),
                containerColor = DarkSurfaceVariant,
                contentColor = CyberCyan,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.EditLocationAlt,
                    contentDescription = "Input Coordinates",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Save to Favorites
            FloatingActionButton(
                onClick = { viewModel.setSaveLocationDialogVisible(true) },
                modifier = Modifier.size(44.dp).testTag("save_current_fab"),
                containerColor = DarkSurfaceVariant,
                contentColor = CyberEmerald,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Save to Favorites",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Joystick Toggle FAB
            FloatingActionButton(
                onClick = { viewModel.toggleJoystick() },
                modifier = Modifier.size(44.dp).testTag("toggle_joystick_fab"),
                containerColor = if (showJoystick) CyberCyan else DarkSurfaceVariant,
                contentColor = if (showJoystick) Color(0xFF0F172A) else Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "Toggle Joystick",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Floating Joystick Overlay
        if (showJoystick) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 200.dp, start = 8.dp)
            ) {
                JoystickController(
                    onMove = { bearing, stepMeters ->
                        viewModel.moveJoystick(bearing, stepMeters)
                    },
                    onClose = { viewModel.toggleJoystick() }
                )
            }
        }

        // Bottom Dashboard HUD Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp)
                .testTag("map_bottom_hud_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Top row of HUD: Status Badge + Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (mockState.isMocking) CyberEmerald else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (mockState.isMocking) "MOCKING BẬT" else "MOCKING TẮT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (mockState.isMocking) CyberEmerald else Color.Gray
                        )

                        if (mockState.mode != MockMode.IDLE) {
                            Text(
                                text = " • ${mockState.mode.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }

                    // Speed display
                    Text(
                        text = "${String.format(Locale.US, "%.1f", mockState.speedKmh)} km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Coordinates Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "VĨ ĐỘ (LAT)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.6f", mockState.latitude),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "KINH ĐỘ (LNG)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.6f", mockState.longitude),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "ĐỘ CAO",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${mockState.altitude.toInt()} m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // If running Route or Scheduled mode, show controls (Pause, Next waypoint)
                if (mockState.mode == MockMode.ROUTE || mockState.mode == MockMode.SCHEDULED) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mockState.statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                            if (mockState.waitSecondsRemaining > 0) {
                                Text(
                                    text = "Đang dừng chờ: ${mockState.waitSecondsRemaining}s",
                                    fontSize = 11.sp,
                                    color = Color.Yellow
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { viewModel.togglePause() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (mockState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = "Pause/Resume",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextWaypoint() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Waypoint",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Start / Stop Mocking Toggle Button
                Button(
                    onClick = {
                        if (mockState.isMocking) {
                            viewModel.stopMocking()
                        } else {
                            viewModel.startStaticMocking()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_mocking_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mockState.isMocking) CyberRed else CyberCyan
                    )
                ) {
                    Icon(
                        imageVector = if (mockState.isMocking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mockState.isMocking) "DỪNG GIẢ LẬP GPS" else "BẮT ĐẦU GIẢ LẬP GPS",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Precision Coordinate Dialog
    if (showCoordDialog) {
        CoordinateInputDialog(
            initialLat = mockState.latitude,
            initialLng = mockState.longitude,
            initialAlt = mockState.altitude,
            onConfirm = { lat, lng, alt ->
                viewModel.teleportTo(lat, lng, alt)
                viewModel.setCoordinateDialogVisible(false)
                val js = "javascript:centerMap($lat, $lng);"
                webViewRef?.evaluateJavascript(js, null)
            },
            onDismiss = { viewModel.setCoordinateDialogVisible(false) }
        )
    }

    // Save Location Dialog
    if (showSaveDialog) {
        SaveLocationDialog(
            latitude = mockState.latitude,
            longitude = mockState.longitude,
            altitude = mockState.altitude,
            onSave = { name, desc, category ->
                viewModel.saveLocation(name, desc, mockState.latitude, mockState.longitude, mockState.altitude, category)
            },
            onDismiss = { viewModel.setSaveLocationDialogVisible(false) }
        )
    }
}

/**
 * Generates interactive OpenStreetMap HTML with Leaflet.js
 */
private fun generateMapHtml(initialLat: Double, initialLng: Double): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #0B0F19; }
                .custom-pin {
                    width: 32px;
                    height: 32px;
                    border-radius: 50%;
                    background: #38BDF8;
                    border: 3px solid #FFFFFF;
                    box-shadow: 0 0 12px rgba(56, 189, 248, 0.8);
                    position: relative;
                }
                .custom-pin::after {
                    content: '';
                    position: absolute;
                    width: 10px;
                    height: 10px;
                    background: #0F172A;
                    border-radius: 50%;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                }
                .leaflet-control-attribution { display: none; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map;
                var marker;

                function initMap(lat, lng) {
                    map = L.map('map', {
                        zoomControl: false,
                        attributionControl: false
                    }).setView([lat, lng], 15);

                    // CartoDB Dark Matter / OSM tiles
                    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                        maxZoom: 19
                    }).addTo(map);

                    var pinIcon = L.divIcon({
                        className: 'custom-pin-container',
                        html: '<div class="custom-pin"></div>',
                        iconSize: [32, 32],
                        iconAnchor: [16, 16]
                    });

                    marker = L.marker([lat, lng], {
                        icon: pinIcon,
                        draggable: true
                    }).addTo(map);

                    marker.on('dragend', function(e) {
                        var pos = marker.getLatLng();
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onLocationSelected(pos.lat, pos.lng);
                        }
                    });

                    map.on('click', function(e) {
                        marker.setLatLng(e.latlng);
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onLocationSelected(e.latlng.lat, e.latlng.lng);
                        }
                    });
                }

                function updateMockMarker(lat, lng, bearing) {
                    if (marker) {
                        marker.setLatLng([lat, lng]);
                    }
                }

                function centerMap(lat, lng) {
                    if (map) {
                        map.panTo([lat, lng], { animate: true });
                        if (marker) marker.setLatLng([lat, lng]);
                    }
                }

                document.addEventListener("DOMContentLoaded", function() {
                    initMap($initialLat, $initialLng);
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}
