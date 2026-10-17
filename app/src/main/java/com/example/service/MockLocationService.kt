package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.RouteSchedule
import com.example.data.model.Waypoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MockLocationService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var mockLoopJob: Job? = null

    private lateinit var providerHelper: MockLocationProviderHelper
    private val moshi = Moshi.Builder().build()
    private val waypointListType = Types.newParameterizedType(List::class.java, Waypoint::class.java)
    private val waypointAdapter = moshi.adapter<List<Waypoint>>(waypointListType)

    // Route tracking state
    private var activeWaypoints: List<Waypoint> = emptyList()
    private var currentSegmentIndex = 0
    private var segmentProgressMeters = 0.0
    private var isMovingForward = true
    private var waitSecondsCounter = 0

    companion object {
        private const val TAG = "MockLocationService"
        const val CHANNEL_ID = "gps_mock_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_STATIC = "com.example.ACTION_START_STATIC"
        const val ACTION_START_ROUTE = "com.example.ACTION_START_ROUTE"
        const val ACTION_START_SCHEDULE = "com.example.ACTION_START_SCHEDULE"
        const val ACTION_UPDATE_COORDS = "com.example.ACTION_UPDATE_COORDS"
        const val ACTION_JOYSTICK_MOVE = "com.example.ACTION_JOYSTICK_MOVE"
        const val ACTION_TOGGLE_PAUSE = "com.example.ACTION_TOGGLE_PAUSE"
        const val ACTION_NEXT_WAYPOINT = "com.example.ACTION_NEXT_WAYPOINT"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val EXTRA_ALT = "extra_alt"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_BEARING = "extra_bearing"
        const val EXTRA_STEP_METERS = "extra_step_meters"
        const val EXTRA_ROUTE_JSON = "extra_route_json"
        const val EXTRA_ROUTE_NAME = "extra_route_name"
        const val EXTRA_LOOP_MODE = "extra_loop_mode"
        const val EXTRA_SCHEDULE_TYPE = "extra_schedule_type"
        const val EXTRA_INTERVAL = "extra_interval"

        fun startStatic(context: Context, lat: Double, lng: Double, alt: Double = 10.0) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_START_STATIC
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
                putExtra(EXTRA_ALT, alt)
            }
            startServiceCompat(context, intent)
        }

        fun updateLocation(context: Context, lat: Double, lng: Double) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_UPDATE_COORDS
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
            }
            startServiceCompat(context, intent)
        }

        fun moveJoystick(context: Context, bearing: Float, stepMeters: Double) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_JOYSTICK_MOVE
                putExtra(EXTRA_BEARING, bearing)
                putExtra(EXTRA_STEP_METERS, stepMeters)
            }
            startServiceCompat(context, intent)
        }

        fun startRoute(context: Context, route: RouteSchedule) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = if (route.scheduleType == "TIMED_HOP") ACTION_START_SCHEDULE else ACTION_START_ROUTE
                putExtra(EXTRA_ROUTE_NAME, route.name)
                putExtra(EXTRA_ROUTE_JSON, route.waypointsJson)
                putExtra(EXTRA_SPEED, route.speedKmh)
                putExtra(EXTRA_INTERVAL, route.intervalSeconds)
                putExtra(EXTRA_LOOP_MODE, route.loopMode)
                putExtra(EXTRA_SCHEDULE_TYPE, route.scheduleType)
            }
            startServiceCompat(context, intent)
        }

        fun togglePause(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_TOGGLE_PAUSE
            }
            startServiceCompat(context, intent)
        }

        fun nextWaypoint(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_NEXT_WAYPOINT
            }
            startServiceCompat(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_STOP
            }
            startServiceCompat(context, intent)
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        providerHelper = MockLocationProviderHelper(this)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_STOP -> {
                stopMocking()
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_TOGGLE_PAUSE -> {
                val current = MockEngineState.state.value
                val newPaused = !current.isPaused
                MockEngineState.update { it.copy(isPaused = newPaused) }
                updateNotification()
                return START_STICKY
            }

            ACTION_NEXT_WAYPOINT -> {
                advanceToNextWaypointManual()
                return START_STICKY
            }

            ACTION_JOYSTICK_MOVE -> {
                val bearing = intent.getFloatExtra(EXTRA_BEARING, 0f)
                val stepMeters = intent.getDoubleExtra(EXTRA_STEP_METERS, 2.0)
                handleJoystickMove(bearing, stepMeters)
                return START_STICKY
            }

            ACTION_UPDATE_COORDS -> {
                val lat = intent.getDoubleExtra(EXTRA_LAT, MockEngineState.state.value.latitude)
                val lng = intent.getDoubleExtra(EXTRA_LNG, MockEngineState.state.value.longitude)
                handleUpdateCoords(lat, lng)
                return START_STICKY
            }

            ACTION_START_STATIC -> {
                val lat = intent.getDoubleExtra(EXTRA_LAT, 21.028511)
                val lng = intent.getDoubleExtra(EXTRA_LNG, 105.854167)
                val alt = intent.getDoubleExtra(EXTRA_ALT, 10.0)
                startStaticMocking(lat, lng, alt)
            }

            ACTION_START_ROUTE, ACTION_START_SCHEDULE -> {
                val routeName = intent.getStringExtra(EXTRA_ROUTE_NAME) ?: "Lộ trình giả lập"
                val routeJson = intent.getStringExtra(EXTRA_ROUTE_JSON) ?: "[]"
                val speed = intent.getFloatExtra(EXTRA_SPEED, 20f)
                val interval = intent.getIntExtra(EXTRA_INTERVAL, 2).coerceAtLeast(1)
                val loopMode = intent.getStringExtra(EXTRA_LOOP_MODE) ?: "LOOP"
                val isTimedHop = action == ACTION_START_SCHEDULE

                startRouteMocking(routeName, routeJson, speed, interval, loopMode, isTimedHop)
            }
        }

        return START_STICKY
    }

    private fun startStaticMocking(lat: Double, lng: Double, alt: Double) {
        val err = providerHelper.setupMockProviders()
        if (err != null) {
            MockEngineState.setError(err)
            stopSelf()
            return
        }

        MockEngineState.setError(null)
        MockEngineState.setLocation(lat, lng, alt, bearing = 0f, speedKmh = 0f)
        MockEngineState.setMocking(true, MockMode.STATIC)

        startAsForeground()

        mockLoopJob?.cancel()
        mockLoopJob = serviceScope.launch {
            while (isActive) {
                val state = MockEngineState.state.value
                val (pubLat, pubLng) = LocationMath.applyJitter(
                    state.latitude,
                    state.longitude,
                    state.realisticJitterEnabled
                )
                providerHelper.publishLocation(
                    pubLat,
                    pubLng,
                    state.altitude,
                    speedMps = 0f,
                    bearingDeg = state.bearing,
                    accuracyMeters = state.accuracy
                )
                delay(1000)
            }
        }
    }

    private fun handleUpdateCoords(lat: Double, lng: Double) {
        MockEngineState.setLocation(lat, lng)
        providerHelper.publishLocation(lat, lng, MockEngineState.state.value.altitude)
        updateNotification()
    }

    private fun handleJoystickMove(bearing: Float, stepMeters: Double) {
        val cur = MockEngineState.state.value
        val (newLat, newLng) = LocationMath.moveByBearing(cur.latitude, cur.longitude, bearing, stepMeters)
        val speedKmh = (stepMeters / 1.0 * 3.6).toFloat() // Approx speed for 1s step
        MockEngineState.setLocation(newLat, newLng, cur.altitude, bearing = bearing, speedKmh = speedKmh)
        providerHelper.publishLocation(
            newLat,
            newLng,
            cur.altitude,
            speedMps = (stepMeters / 1.0).toFloat(),
            bearingDeg = bearing,
            accuracyMeters = cur.accuracy
        )
        updateNotification()
    }

    private fun startRouteMocking(
        routeName: String,
        routeJson: String,
        speedKmh: Float,
        intervalSeconds: Int,
        loopMode: String,
        isTimedHop: Boolean
    ) {
        val waypoints = try {
            waypointAdapter.fromJson(routeJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (waypoints.isEmpty()) {
            MockEngineState.setError("Lộ trình không có tọa độ nào!")
            stopSelf()
            return
        }

        val err = providerHelper.setupMockProviders()
        if (err != null) {
            MockEngineState.setError(err)
            stopSelf()
            return
        }

        MockEngineState.setError(null)
        activeWaypoints = waypoints
        currentSegmentIndex = 0
        segmentProgressMeters = 0.0
        isMovingForward = true
        waitSecondsCounter = 0

        val mode = if (isTimedHop) MockMode.SCHEDULED else MockMode.ROUTE
        val initialWp = waypoints.first()
        MockEngineState.setLocation(
            initialWp.latitude,
            initialWp.longitude,
            initialWp.altitude,
            bearing = 0f,
            speedKmh = speedKmh
        )
        MockEngineState.update {
            it.copy(
                isMocking = true,
                mode = mode,
                activeRoute = RouteSchedule(
                    name = routeName,
                    speedKmh = speedKmh,
                    intervalSeconds = intervalSeconds,
                    loopMode = loopMode,
                    scheduleType = if (isTimedHop) "TIMED_HOP" else "CONTINUOUS",
                    waypointsJson = routeJson
                ),
                currentWaypointIndex = 0,
                totalWaypoints = waypoints.size,
                statusText = "Đang chạy lộ trình: $routeName"
            )
        }

        startAsForeground()

        mockLoopJob?.cancel()
        mockLoopJob = serviceScope.launch {
            if (isTimedHop) {
                runTimedScheduleLoop(intervalSeconds, loopMode)
            } else {
                runContinuousRouteLoop(speedKmh, intervalSeconds, loopMode)
            }
        }
    }

    private suspend fun runContinuousRouteLoop(speedKmh: Float, intervalSeconds: Int, loopMode: String) {
        val speedMps = (speedKmh * 1000.0 / 3600.0).coerceAtLeast(0.5)

        while (mockLoopJob?.isActive == true) {
            if (MockEngineState.state.value.isPaused) {
                delay(1000)
                continue
            }

            if (activeWaypoints.size < 2) {
                val single = activeWaypoints.first()
                providerHelper.publishLocation(single.latitude, single.longitude, single.altitude)
                delay(intervalSeconds * 1000L)
                continue
            }

            // Check if currently waiting at waypoint
            if (waitSecondsCounter > 0) {
                MockEngineState.update { it.copy(waitSecondsRemaining = waitSecondsCounter) }
                updateNotification()
                delay(1000)
                waitSecondsCounter--
                continue
            } else {
                MockEngineState.update { it.copy(waitSecondsRemaining = 0) }
            }

            val p1 = activeWaypoints[currentSegmentIndex]
            val nextIdx = if (isMovingForward) currentSegmentIndex + 1 else currentSegmentIndex - 1
            val p2 = activeWaypoints[nextIdx]

            val segmentLength = LocationMath.distanceMeters(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
            val stepMeters = speedMps * intervalSeconds

            segmentProgressMeters += stepMeters

            if (segmentProgressMeters >= segmentLength || segmentLength <= 1.0) {
                // Reached next waypoint!
                currentSegmentIndex = nextIdx
                segmentProgressMeters = 0.0
                val targetWp = activeWaypoints[currentSegmentIndex]
                waitSecondsCounter = targetWp.waitSeconds

                // Determine next destination segment according to loop mode
                checkNextSegmentBoundary(loopMode)

                MockEngineState.setLocation(
                    targetWp.latitude,
                    targetWp.longitude,
                    targetWp.altitude,
                    speedKmh = speedKmh
                )
                MockEngineState.update {
                    it.copy(
                        currentWaypointIndex = currentSegmentIndex,
                        statusText = "Đến điểm: ${targetWp.name.ifEmpty { "Điểm ${currentSegmentIndex + 1}" }}"
                    )
                }

                providerHelper.publishLocation(
                    targetWp.latitude,
                    targetWp.longitude,
                    targetWp.altitude,
                    speedMps.toFloat(),
                    0f
                )
            } else {
                // Interpolate along segment
                val fraction = (segmentProgressMeters / segmentLength).coerceIn(0.0, 1.0)
                val (curLat, curLng) = LocationMath.interpolate(p1.latitude, p1.longitude, p2.latitude, p2.longitude, fraction)
                val bearing = LocationMath.bearingDegrees(p1.latitude, p1.longitude, p2.latitude, p2.longitude)

                MockEngineState.setLocation(curLat, curLng, p1.altitude, bearing = bearing, speedKmh = speedKmh)
                MockEngineState.update {
                    it.copy(currentWaypointIndex = currentSegmentIndex)
                }

                providerHelper.publishLocation(
                    curLat,
                    curLng,
                    p1.altitude,
                    speedMps.toFloat(),
                    bearing
                )
            }

            updateNotification()
            delay(intervalSeconds * 1000L)
        }
    }

    private suspend fun runTimedScheduleLoop(intervalSeconds: Int, loopMode: String) {
        while (mockLoopJob?.isActive == true) {
            if (MockEngineState.state.value.isPaused) {
                delay(1000)
                continue
            }

            val currentWp = activeWaypoints[currentSegmentIndex]
            MockEngineState.setLocation(currentWp.latitude, currentWp.longitude, currentWp.altitude)
            MockEngineState.update {
                it.copy(
                    currentWaypointIndex = currentSegmentIndex,
                    statusText = "Tại điểm: ${currentWp.name.ifEmpty { "Điểm ${currentSegmentIndex + 1}" }}"
                )
            }

            val stayDuration = if (currentWp.waitSeconds > 0) currentWp.waitSeconds else 30
            for (remaining in stayDuration downTo 1) {
                if (!mockLoopJob?.isActive!!) break
                while (MockEngineState.state.value.isPaused) {
                    delay(1000)
                }
                MockEngineState.update { it.copy(waitSecondsRemaining = remaining) }
                providerHelper.publishLocation(currentWp.latitude, currentWp.longitude, currentWp.altitude)
                updateNotification()
                delay(1000)
            }

            // Move to next scheduled point
            checkNextSegmentBoundary(loopMode)
            currentSegmentIndex = if (isMovingForward) (currentSegmentIndex + 1) else (currentSegmentIndex - 1)
            currentSegmentIndex = currentSegmentIndex.coerceIn(0, activeWaypoints.size - 1)
        }
    }

    private fun checkNextSegmentBoundary(loopMode: String) {
        val lastIdx = activeWaypoints.size - 1
        if (isMovingForward && currentSegmentIndex >= lastIdx) {
            when (loopMode) {
                "LOOP" -> {
                    currentSegmentIndex = 0
                }
                "PING_PONG" -> {
                    isMovingForward = false
                }
                "ONCE" -> {
                    MockEngineState.update { it.copy(statusText = "Lộ trình đã hoàn thành!", isPaused = true) }
                }
            }
        } else if (!isMovingForward && currentSegmentIndex <= 0) {
            isMovingForward = true
        }
    }

    private fun advanceToNextWaypointManual() {
        if (activeWaypoints.isEmpty()) return
        currentSegmentIndex = (currentSegmentIndex + 1) % activeWaypoints.size
        segmentProgressMeters = 0.0
        waitSecondsCounter = 0
        val wp = activeWaypoints[currentSegmentIndex]
        MockEngineState.setLocation(wp.latitude, wp.longitude, wp.altitude)
        MockEngineState.update { it.copy(currentWaypointIndex = currentSegmentIndex) }
        providerHelper.publishLocation(wp.latitude, wp.longitude, wp.altitude)
        updateNotification()
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val state = MockEngineState.state.value
        val latStr = String.format(Locale.US, "%.5f", state.latitude)
        val lngStr = String.format(Locale.US, "%.5f", state.longitude)

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MockLocationService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, MockLocationService::class.java).apply { action = ACTION_TOGGLE_PAUSE }
        val pausePending = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MockLocationService::class.java).apply { action = ACTION_NEXT_WAYPOINT }
        val nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val statusInfo = when (state.mode) {
            MockMode.ROUTE -> "Lộ trình (${state.currentWaypointIndex + 1}/${state.totalWaypoints}) • ${String.format(Locale.US, "%.1f", state.speedKmh)} km/h"
            MockMode.SCHEDULED -> "Lịch trình (${state.currentWaypointIndex + 1}/${state.totalWaypoints}) • Còn ${state.waitSecondsRemaining}s"
            MockMode.STATIC -> "Vị trí cố định"
            MockMode.IDLE -> "Đang dừng"
        }

        val pauseTitle = if (state.isPaused) "Tiếp tục" else "Tạm dừng"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Mock: $statusInfo")
            .setContentText("$latStr, $lngStr")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(activityPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, pauseTitle, pausePending)
            .addAction(android.R.drawable.ic_media_next, "Điểm tiếp", nextPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dừng", stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    private fun stopMocking() {
        mockLoopJob?.cancel()
        providerHelper.cleanup()
        MockEngineState.setMocking(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopMocking()
        Log.d(TAG, "MockLocationService destroyed")
    }
}
