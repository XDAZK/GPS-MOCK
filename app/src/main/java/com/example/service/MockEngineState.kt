package com.example.service

import com.example.data.model.RouteSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MockMode {
    IDLE,
    STATIC,
    ROUTE,
    SCHEDULED
}

data class MockEngineData(
    val isMocking: Boolean = false,
    val isPaused: Boolean = false,
    val latitude: Double = 21.028511, // Default Hanoi
    val longitude: Double = 105.854167,
    val altitude: Double = 10.0,
    val speedKmh: Float = 0f,
    val bearing: Float = 0f,
    val accuracy: Float = 2.0f,
    val mode: MockMode = MockMode.IDLE,
    val activeRoute: RouteSchedule? = null,
    val currentWaypointIndex: Int = 0,
    val totalWaypoints: Int = 0,
    val waitSecondsRemaining: Int = 0,
    val statusText: String = "Ready to mock",
    val hasMockPermission: Boolean = true,
    val lastError: String? = null,
    val realisticJitterEnabled: Boolean = true
)

object MockEngineState {
    private val _state = MutableStateFlow(MockEngineData())
    val state: StateFlow<MockEngineData> = _state.asStateFlow()

    fun update(transform: (MockEngineData) -> MockEngineData) {
        _state.update(transform)
    }

    fun setLocation(lat: Double, lng: Double, alt: Double = 10.0, bearing: Float = _state.value.bearing, speedKmh: Float = _state.value.speedKmh) {
        _state.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                altitude = alt,
                bearing = bearing,
                speedKmh = speedKmh
            )
        }
    }

    fun setMocking(active: Boolean, mode: MockMode = MockMode.STATIC, route: RouteSchedule? = null) {
        _state.update {
            it.copy(
                isMocking = active,
                mode = if (active) mode else MockMode.IDLE,
                activeRoute = if (active) route else null,
                isPaused = false,
                statusText = if (active) "Mocking Active ($mode)" else "Mocking Stopped"
            )
        }
    }

    fun setError(err: String?) {
        _state.update { it.copy(lastError = err, hasMockPermission = err == null) }
    }
}
