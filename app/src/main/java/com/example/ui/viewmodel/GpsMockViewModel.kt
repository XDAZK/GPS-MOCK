package com.example.ui.viewmodel

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GpsMockApplication
import com.example.data.model.MockHistoryEntry
import com.example.data.model.RouteSchedule
import com.example.data.model.SavedLocation
import com.example.data.model.Waypoint
import com.example.service.MockEngineData
import com.example.service.MockEngineState
import com.example.service.MockLocationProviderHelper
import com.example.service.MockLocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import org.json.JSONArray

data class SearchResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

class GpsMockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as GpsMockApplication).repository
    private val providerHelper = MockLocationProviderHelper(application)

    val mockEngineState: StateFlow<MockEngineData> = MockEngineState.state

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _showJoystick = MutableStateFlow(false)
    val showJoystick: StateFlow<Boolean> = _showJoystick.asStateFlow()

    private val _showCoordinateDialog = MutableStateFlow(false)
    val showCoordinateDialog: StateFlow<Boolean> = _showCoordinateDialog.asStateFlow()

    private val _showSaveLocationDialog = MutableStateFlow(false)
    val showSaveLocationDialog: StateFlow<Boolean> = _showSaveLocationDialog.asStateFlow()

    private val _showCreateRouteDialog = MutableStateFlow(false)
    val showCreateRouteDialog: StateFlow<Boolean> = _showCreateRouteDialog.asStateFlow()

    private val _showDevOptionsPrompt = MutableStateFlow(false)
    val showDevOptionsPrompt: StateFlow<Boolean> = _showDevOptionsPrompt.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var currentSessionId: Long? = null
    private var sessionStartTime: Long = 0

    // Filtered saved locations
    val savedLocations: StateFlow<List<SavedLocation>> = combine(
        repository.allSavedLocations,
        _selectedCategory,
        _searchQuery
    ) { list, category, query ->
        list.filter { item ->
            val matchCategory = category == "All" || item.category.equals(category, ignoreCase = true)
            val matchQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routeSchedules: StateFlow<List<RouteSchedule>> = repository.allRouteSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyEntries: StateFlow<List<MockHistoryEntry>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun toggleJoystick() {
        _showJoystick.value = !_showJoystick.value
    }

    fun setCoordinateDialogVisible(visible: Boolean) {
        _showCoordinateDialog.value = visible
    }

    fun setSaveLocationDialogVisible(visible: Boolean) {
        _showSaveLocationDialog.value = visible
    }

    fun setCreateRouteDialogVisible(visible: Boolean) {
        _showCreateRouteDialog.value = visible
    }

    fun setDevOptionsPromptVisible(visible: Boolean) {
        _showDevOptionsPrompt.value = visible
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun checkMockPermission(): Boolean {
        val allowed = providerHelper.isMockAppAllowed()
        MockEngineState.update { it.copy(hasMockPermission = allowed) }
        return allowed
    }

    fun startStaticMocking() {
        val current = MockEngineState.state.value
        MockLocationService.startStatic(
            getApplication(),
            current.latitude,
            current.longitude,
            current.altitude
        )

        sessionStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            val entry = MockHistoryEntry(
                mode = "fixed",
                label = String.format(Locale.US, "%.5f, %.5f", current.latitude, current.longitude),
                latitude = current.latitude,
                longitude = current.longitude,
                startedAt = sessionStartTime
            )
            currentSessionId = repository.addHistoryEntry(entry)
        }
    }

    fun stopMocking() {
        MockLocationService.stop(getApplication())
        val sessionId = currentSessionId
        if (sessionId != null) {
            val durationSec = ((System.currentTimeMillis() - sessionStartTime) / 1000).coerceAtLeast(1)
            viewModelScope.launch {
                repository.finishHistoryEntry(sessionId, durationSec)
                currentSessionId = null
            }
        }
    }

    fun teleportTo(lat: Double, lng: Double, alt: Double = 10.0, name: String? = null) {
        val current = MockEngineState.state.value
        MockEngineState.setLocation(lat, lng, alt)
        if (current.isMocking) {
            MockLocationService.updateLocation(getApplication(), lat, lng)
        }
        _userMessage.value = "Đã chọn: ${name ?: String.format(Locale.US, "%.5f, %.5f", lat, lng)}"
    }

    fun moveJoystick(bearing: Float, stepMeters: Double) {
        MockLocationService.moveJoystick(getApplication(), bearing, stepMeters)
    }

    fun togglePause() {
        MockLocationService.togglePause(getApplication())
    }

    fun nextWaypoint() {
        MockLocationService.nextWaypoint(getApplication())
    }

    fun startRoute(route: RouteSchedule) {
        MockLocationService.startRoute(getApplication(), route)
        _userMessage.value = "Bắt đầu lộ trình: ${route.name}"

        sessionStartTime = System.currentTimeMillis()
        val waypoints = parseWaypoints(route.waypointsJson)
        val firstWp = waypoints.firstOrNull()
        viewModelScope.launch {
            val entry = MockHistoryEntry(
                mode = "route",
                label = route.name,
                latitude = firstWp?.latitude ?: 0.0,
                longitude = firstWp?.longitude ?: 0.0,
                startedAt = sessionStartTime
            )
            currentSessionId = repository.addHistoryEntry(entry)
        }
    }

    fun saveLocation(
        name: String,
        description: String,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        category: String
    ) {
        viewModelScope.launch {
            val item = SavedLocation(
                name = name.ifBlank { "Vị trí đã lưu" },
                description = description,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                category = category.ifBlank { "Favorites" },
                isPreset = false
            )
            repository.saveLocation(item)
            _userMessage.value = "Đã lưu: ${item.name}"
            _showSaveLocationDialog.value = false
        }
    }

    fun deleteSavedLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            _userMessage.value = "Đã xóa ${location.name}"
        }
    }

    fun createRoute(
        name: String,
        description: String,
        speedKmh: Float,
        intervalSeconds: Int,
        loopMode: String,
        scheduleType: String,
        waypoints: List<Waypoint>
    ) {
        viewModelScope.launch {
            val json = repository.serializeWaypoints(waypoints)
            val route = RouteSchedule(
                name = name.ifBlank { "Lộ trình mới" },
                description = description,
                speedKmh = speedKmh,
                intervalSeconds = intervalSeconds,
                loopMode = loopMode,
                scheduleType = scheduleType,
                waypointsJson = json
            )
            repository.saveRoute(route)
            _userMessage.value = "Đã tạo lộ trình: ${route.name}"
            _showCreateRouteDialog.value = false
        }
    }

    fun deleteRoute(route: RouteSchedule) {
        viewModelScope.launch {
            repository.deleteRoute(route)
            _userMessage.value = "Đã xóa lộ trình ${route.name}"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userMessage.value = "Đã xóa lịch sử"
        }
    }

    fun parseWaypoints(json: String): List<Waypoint> {
        return repository.parseWaypoints(json)
    }

    fun searchPlaces(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            val results = withContext(Dispatchers.IO) {
                searchNominatim(query)
            }
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    private fun searchNominatim(query: String): List<SearchResult> {
        val list = mutableListOf<SearchResult>()
        try {
            val geocoder = Geocoder(getApplication(), Locale.getDefault())
            if (Geocoder.isPresent()) {
                val addresses = geocoder.getFromLocationName(query, 5)
                if (!addresses.isNullOrEmpty()) {
                    for (addr in addresses) {
                        val name = (0..addr.maxAddressLineIndex).joinToString(", ") { addr.getAddressLine(it) }
                        list.add(SearchResult(name.ifBlank { query }, addr.latitude, addr.longitude))
                    }
                    if (list.isNotEmpty()) return list
                }
            }

            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "GpsMockAndroidApp/1.0")
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val displayName = obj.optString("display_name", query)
                val lat = obj.optDouble("lat", 0.0)
                val lon = obj.optDouble("lon", 0.0)
                list.add(SearchResult(displayName, lat, lon))
            }
        } catch (e: Exception) {
            // Network fallback
        }
        return list
    }

    fun setRealisticJitter(enabled: Boolean) {
        MockEngineState.update { it.copy(realisticJitterEnabled = enabled) }
    }

    fun setAccuracy(accuracy: Float) {
        MockEngineState.update { it.copy(accuracy = accuracy) }
    }
}
