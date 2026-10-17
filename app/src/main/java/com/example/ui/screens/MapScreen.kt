package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
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
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val showJoystick by viewModel.showJoystick.collectAsStateWithLifecycle()
    val showCoordDialog by viewModel.showCoordinateDialog.collectAsStateWithLifecycle()
    val showSaveDialog by viewModel.showSaveLocationDialog.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var isDeckExpanded by remember { mutableStateOf(false) }
    var isDarkTiles by remember { mutableStateOf(true) }

    // Check if current location matches any saved favorite
    val isFavorite = savedLocations.any {
        Math.abs(it.latitude - mockState.latitude) < 0.0001 &&
        Math.abs(it.longitude - mockState.longitude) < 0.0001
    }

    // Push coordinates to Leaflet whenever location changes
    LaunchedEffect(mockState.latitude, mockState.longitude, isMapLoaded) {
        if (isMapLoaded && webViewRef != null) {
            val js = "javascript:if(window.updateMockMarker) window.updateMockMarker(${mockState.latitude}, ${mockState.longitude}, ${mockState.bearing});"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("map_screen_container")) {

        // Full-bleed Map View: loads local standalone Leaflet engine
        AndroidView(
            modifier = Modifier.fillMaxSize().testTag("leaflet_map_view"),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccessFromFileURLs = true
                    settings.allowUniversalAccessFromFileURLs = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.cacheMode = WebSettings.LOAD_DEFAULT

                    addJavascriptInterface(
                        WebAppInterface { lat, lng ->
                            viewModel.teleportTo(lat, lng)
                        },
                        "AndroidBridge"
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                            Log.d("GPSMockMap", "${msg?.message()} -- line ${msg?.lineNumber()}")
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                            val js = "javascript:initMap(${mockState.latitude}, ${mockState.longitude});"
                            evaluateJavascript(js, null)
                        }
                    }

                    // Load self-contained inlined map HTML with OSM origin for flawless tile loading
                    val htmlContent = try {
                        ctx.assets.open("leaflet/map.html").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        Log.e("GPSMockMap", "Failed to read leaflet/map.html", e)
                        ""
                    }
                    loadDataWithBaseURL("https://tile.openstreetmap.org/", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            }
        )

        // Top Floating Search Bar (matching Sriharan-S/gps-mock PlaceSearchBar)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.94f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("place_search_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
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
                        placeholder = { Text("Search location or coordinates...", fontSize = 13.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
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

            // Search Results Dropdown
            AnimatedVisibility(visible = searchResults.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
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

        // Right Control Rail (matching Sriharan-S/gps-mock)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter Camera on Mock Pin
            FloatingActionButton(
                onClick = {
                    val js = "javascript:centerMap(${mockState.latitude}, ${mockState.longitude});"
                    webViewRef?.evaluateJavascript(js, null)
                },
                modifier = Modifier.size(42.dp).testTag("fab_recenter"),
                containerColor = DarkSurface.copy(alpha = 0.9f),
                contentColor = CyberCyan,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on Pin", modifier = Modifier.size(20.dp))
            }

            // Direct Coordinate Input
            FloatingActionButton(
                onClick = { viewModel.setCoordinateDialogVisible(true) },
                modifier = Modifier.size(42.dp).testTag("fab_coordinates"),
                containerColor = DarkSurface.copy(alpha = 0.9f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.EditLocationAlt, contentDescription = "Input Coords", modifier = Modifier.size(20.dp))
            }

            // Joystick Toggle
            FloatingActionButton(
                onClick = { viewModel.toggleJoystick() },
                modifier = Modifier.size(42.dp).testTag("fab_joystick"),
                containerColor = if (showJoystick) CyberCyan else DarkSurface.copy(alpha = 0.9f),
                contentColor = if (showJoystick) Color(0xFF0F172A) else Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Gamepad, contentDescription = "Toggle Joystick", modifier = Modifier.size(20.dp))
            }

            // Toggle Dark / Normal Tile Filter
            FloatingActionButton(
                onClick = {
                    isDarkTiles = !isDarkTiles
                    val js = "javascript:toggleMapStyle($isDarkTiles);"
                    webViewRef?.evaluateJavascript(js, null)
                },
                modifier = Modifier.size(42.dp),
                containerColor = DarkSurface.copy(alpha = 0.9f),
                contentColor = Color.LightGray,
                shape = CircleShape
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = "Map Style", modifier = Modifier.size(20.dp))
            }
        }

        // Floating Joystick Overlay
        if (showJoystick) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 120.dp, start = 8.dp)
            ) {
                JoystickController(
                    onMove = { bearing, stepMeters ->
                        viewModel.moveJoystick(bearing, stepMeters)
                    },
                    onClose = { viewModel.toggleJoystick() }
                )
            }
        }

        // Bottom Control Deck (matching Sriharan-S/gps-mock ControlDeck bottom sheet)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .animateContentSize(animationSpec = tween(240))
                .testTag("control_deck"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Grabber handle for expand/collapse
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDeckExpanded = !isDeckExpanded }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                }

                // PEEK BAR: Compact line with Mode Icon, Coords/Address, Heart Button, and Primary Action Orb
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Icon Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (mockState.isMocking) CyberEmerald.copy(alpha = 0.2f) else DarkSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mockState.mode == MockMode.ROUTE) Icons.Default.Route else Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (mockState.isMocking) CyberEmerald else CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Coordinates
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isDeckExpanded = !isDeckExpanded }
                    ) {
                        Text(
                            text = if (mockState.isMocking) "GPS Mock Active" else "Mock Location Ready",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (mockState.isMocking) CyberEmerald else Color.White
                        )
                        Text(
                            text = String.format(Locale.US, "%.5f, %.5f • Alt: %dm", mockState.latitude, mockState.longitude, mockState.altitude.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Save / Favorite Heart Button
                    IconButton(
                        onClick = { viewModel.setSaveLocationDialogVisible(true) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Primary Action Orb (Large round start/stop mocking button matching _PrimaryOrb in Flutter repo)
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (mockState.isMocking) {
                                    viewModel.stopMocking()
                                } else {
                                    viewModel.startStaticMocking()
                                }
                            }
                            .testTag("primary_action_orb"),
                        color = if (mockState.isMocking) CyberRed else CyberCyan,
                        shape = CircleShape,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (mockState.isMocking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (mockState.isMocking) "Stop Mocking" else "Start Mocking",
                                tint = if (mockState.isMocking) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // EXPANDED DECK DETAILS: Exposed when user taps or expands the control deck
                AnimatedVisibility(visible = isDeckExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectTab(1) },
                                color = DarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Favorites", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectTab(2) },
                                color = DarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Route, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lập Lịch Trình", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }

                        // Route Progress Bar (if route mode is active)
                        if (mockState.mode == MockMode.ROUTE || mockState.mode == MockMode.SCHEDULED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(10.dp),
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
                                            text = "Dừng chờ: ${mockState.waitSecondsRemaining}s",
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
                                            contentDescription = null,
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
                                            contentDescription = null,
                                            tint = CyberCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
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
