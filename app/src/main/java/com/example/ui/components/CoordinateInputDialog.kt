package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import java.util.Locale

@Composable
fun CoordinateInputDialog(
    initialLat: Double,
    initialLng: Double,
    initialAlt: Double,
    onConfirm: (lat: Double, lng: Double, alt: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var latText by remember { mutableStateOf(String.format(Locale.US, "%.6f", initialLat)) }
    var lngText by remember { mutableStateOf(String.format(Locale.US, "%.6f", initialLng)) }
    var altText by remember { mutableStateOf(String.format(Locale.US, "%.1f", initialAlt)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PinDrop,
                contentDescription = "Coordinates",
                tint = CyberCyan
            )
        },
        title = {
            Text(
                text = "Nhập tọa độ chính xác",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latText,
                    onValueChange = {
                        latText = it
                        errorMsg = null
                    },
                    label = { Text("Vĩ độ (Latitude: -90 đến 90)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_latitude_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lngText,
                    onValueChange = {
                        lngText = it
                        errorMsg = null
                    },
                    label = { Text("Kinh độ (Longitude: -180 đến 180)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_longitude_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = altText,
                    onValueChange = {
                        altText = it
                        errorMsg = null
                    },
                    label = { Text("Độ cao (Altitude: mét)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_altitude_field")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    val alt = altText.toDoubleOrNull() ?: 0.0

                    if (lat == null || lat < -90.0 || lat > 90.0) {
                        errorMsg = "Vĩ độ không hợp lệ (-90 đến 90)"
                        return@Button
                    }
                    if (lng == null || lng < -180.0 || lng > 180.0) {
                        errorMsg = "Kinh độ không hợp lệ (-180 đến 180)"
                        return@Button
                    }

                    onConfirm(lat, lng, alt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("confirm_coordinates_button")
            ) {
                Text("Dịch chuyển", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_coordinates_button")
            ) {
                Text("Hủy")
            }
        }
    )
}
