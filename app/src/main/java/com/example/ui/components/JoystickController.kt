package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurfaceVariant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JoystickController(
    onMove: (bearing: Float, stepMeters: Double) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var speedLevel by remember { mutableFloatStateOf(10f) } // Speed in km/h
    var thumbOffsetX by remember { mutableFloatStateOf(0f) }
    var thumbOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 110f

    Card(
        modifier = modifier
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .testTag("joystick_controller_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.width(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Joystick",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Joystick",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp).testTag("close_joystick_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Joystick",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Joystick Ring & Thumbstick
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .pointerInput(speedLevel) {
                        detectDragGestures(
                            onDragEnd = {
                                thumbOffsetX = 0f
                                thumbOffsetY = 0f
                            },
                            onDragCancel = {
                                thumbOffsetX = 0f
                                thumbOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newX = thumbOffsetX + dragAmount.x
                                val newY = thumbOffsetY + dragAmount.y
                                val dist = sqrt(newX * newX + newY * newY)

                                if (dist <= maxRadius) {
                                    thumbOffsetX = newX
                                    thumbOffsetY = newY
                                } else {
                                    val angle = atan2(newY, newX)
                                    thumbOffsetX = (cos(angle) * maxRadius)
                                    thumbOffsetY = (sin(angle) * maxRadius)
                                }

                                if (dist > 15f) {
                                    // Calculate bearing from (0,0) to (thumbOffsetX, thumbOffsetY)
                                    // 0° is North (negative Y), 90° East (positive X), 180° South, 270° West
                                    val rad = atan2(thumbOffsetX.toDouble(), -thumbOffsetY.toDouble())
                                    val bearing = ((Math.toDegrees(rad) + 360.0) % 360.0).toFloat()
                                    // Speed step in meters (approx 1 sec tick)
                                    val stepMeters = (speedLevel * 1000.0 / 3600.0) * (dist / maxRadius)
                                    onMove(bearing, stepMeters)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Cardinal markers (N, S, E, W)
                Text("N", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
                Text("S", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
                Text("W", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp))
                Text("E", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp))

                // Outer guide circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                )

                // Draggable Thumbstick
                Box(
                    modifier = Modifier
                        .offset { IntOffset(thumbOffsetX.roundToInt(), thumbOffsetY.roundToInt()) }
                        .size(46.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .testTag("joystick_thumbstick"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C4A6E))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speed Selector Slider
            Row(
                modifier = Modifier.width(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${speedLevel.toInt()} km/h",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    val isWalk = speedLevel <= 8f
                    val isBike = speedLevel in 9f..25f
                    val isCar = speedLevel > 25f

                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { speedLevel = 5f },
                        color = if (isWalk) CyberCyan.copy(alpha = 0.3f) else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = "Walk", modifier = Modifier.size(16.dp), tint = if (isWalk) CyberCyan else Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { speedLevel = 18f },
                        color = if (isBike) CyberCyan.copy(alpha = 0.3f) else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = "Bike", modifier = Modifier.size(16.dp), tint = if (isBike) CyberCyan else Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { speedLevel = 60f },
                        color = if (isCar) CyberCyan.copy(alpha = 0.3f) else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = "Car", modifier = Modifier.size(16.dp), tint = if (isCar) CyberCyan else Color.Gray)
                        }
                    }
                }
            }

            Slider(
                value = speedLevel,
                onValueChange = { speedLevel = it },
                valueRange = 1f..120f,
                modifier = Modifier.width(180.dp).testTag("joystick_speed_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = CyberCyan,
                    activeTrackColor = CyberCyan,
                    inactiveTrackColor = Color.DarkGray
                )
            )
        }
    }
}
