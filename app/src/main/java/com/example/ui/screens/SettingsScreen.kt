package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.GpsMockViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: GpsMockViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mockState by viewModel.mockEngineState.collectAsStateWithLifecycle()
    var isMockPermAllowed by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isMockPermAllowed = viewModel.checkMockPermission()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cài đặt & Hướng dẫn",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Options Status Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dev_options_status_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMockPermAllowed) DarkSurface else DarkSurfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMockPermAllowed) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isMockPermAllowed) CyberEmerald else CyberOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quyền Ứng dụng Vị trí giả",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMockPermAllowed) CyberEmerald.copy(alpha = 0.2f) else CyberOrange.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isMockPermAllowed) "ĐÃ CẤP QUYỀN" else "CHƯA BẬT",
                            color = if (isMockPermAllowed) CyberEmerald else CyberOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Để các ứng dụng khác nhận diện vị trí giả từ GPS Mock, bạn cần chọn 'GPS Mock' trong Cài đặt cho nhà phát triển.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Steps
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("1. Vào Cài đặt Android > Thông tin điện thoại > Nhấn 7 lần vào 'Số hiệu bản dựng' (Build number).", fontSize = 11.sp, color = Color.LightGray)
                    Text("2. Mở 'Tùy chọn nhà phát triển' (Developer Options).", fontSize = 11.sp, color = Color.LightGray)
                    Text("3. Tìm mục 'Chọn ứng dụng vị trí giả' (Select mock location app) > Chọn 'GPS Mock'.", fontSize = 11.sp, color = CyberCyan)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_dev_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mở Tùy chọn nhà phát triển", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulation Realism Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mô phỏng chân thực (Realism)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Jitter Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Độ nhiễu vi mô tự nhiên (GPS Jitter)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Tự động tạo dao động nhẹ (~0.5m) tránh bị các ứng dụng khác phát hiện vị trí tĩnh bất thường.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = mockState.realisticJitterEnabled,
                        onCheckedChange = { viewModel.setRealisticJitter(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Accuracy Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Độ chính xác giả lập (Accuracy):", fontSize = 12.sp, color = Color.White)
                        Text("${String.format(Locale.US, "%.1f", mockState.accuracy)} m", fontSize = 12.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = mockState.accuracy,
                        onValueChange = { viewModel.setAccuracy(it) },
                        valueRange = 1.0f..15.0f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CI/CD Release & App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GitHub CI/CD & Build Release",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Đã tích hợp GitHub Actions workflow (.github/workflows/build-release.yml) sẵn sàng tự động build file release APK khi push code hoặc release tag lên GitHub.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Phiên bản ứng dụng:", fontSize = 12.sp, color = Color.Gray)
                    Text("v1.0.0 (Release Build)", fontSize = 12.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Động cơ Mock:", fontSize = 12.sp, color = Color.Gray)
                    Text("Android TestProvider + Foreground Service", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}
