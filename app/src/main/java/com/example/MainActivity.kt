package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MapScreen
import com.example.ui.screens.RouteScheduleScreen
import com.example.ui.screens.SavedLocationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GpsMockViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GpsMockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    onOpenDeveloperOptions = {
                        try {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                        } catch (e: Exception) {
                            startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkMockPermission()
    }
}

@Composable
fun MainAppContent(
    viewModel: GpsMockViewModel,
    onOpenDeveloperOptions: () -> Unit
) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val mockState by viewModel.mockEngineState.collectAsStateWithLifecycle()
    val showDevPrompt by viewModel.showDevOptionsPrompt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Compose ActivityResult launcher for runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.checkMockPermission()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needed = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
        viewModel.checkMockPermission()
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(mockState.lastError) {
        mockState.lastError?.let {
            viewModel.setDevOptionsPromptVisible(true)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0B0F19),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Bản đồ") },
                    label = { Text("Bản đồ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_map")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Đã lưu") },
                    label = { Text("Đã lưu") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_saved")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.AltRoute, contentDescription = "Lịch trình") },
                    label = { Text("Lịch trình") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_routes")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
                    label = { Text("Cài đặt") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0F172A),
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    0 -> MapScreen(viewModel = viewModel)
                    1 -> SavedLocationsScreen(
                        viewModel = viewModel,
                        onNavigateToMap = { viewModel.selectTab(0) }
                    )
                    2 -> RouteScheduleScreen(
                        viewModel = viewModel,
                        onNavigateToMap = { viewModel.selectTab(0) }
                    )
                    3 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showDevPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.setDevOptionsPromptVisible(false) },
            icon = {
                Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = CyberCyan)
            },
            title = { Text("Cần chọn ứng dụng vị trí giả") },
            text = {
                Text(
                    "GPS Mock cần được chọn làm 'Ứng dụng vị trí giả' trong Tùy chọn nhà phát triển (Developer Options) để hoạt động.\n\n" +
                    "Nhấn nút bên dưới để mở trực tiếp cài đặt."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDevOptionsPromptVisible(false)
                        onOpenDeveloperOptions()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Mở Cài đặt ngay", color = Color(0xFF0F172A))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.setDevOptionsPromptVisible(false) }) {
                    Text("Để sau")
                }
            }
        )
    }
}
