package com.example.service

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log

class MockLocationProviderHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    private var isProviderAdded = false

    companion object {
        private const val TAG = "MockLocationHelper"
    }

    /**
     * Attempts to register mock test providers. Returns null on success, or error message on failure.
     */
    fun setupMockProviders(): String? {
        try {
            for (provider in providers) {
                try {
                    // In case it was already registered
                    locationManager.removeTestProvider(provider)
                } catch (e: Exception) {
                    // Ignore if not present
                }

                locationManager.addTestProvider(
                    provider,
                    false, // requiresNetwork
                    false, // requiresSatellite
                    false, // requiresCell
                    false, // hasMonetaryCost
                    true,  // supportsAltitude
                    true,  // supportsSpeed
                    true,  // supportsBearing
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE
                )
                locationManager.setTestProviderEnabled(provider, true)
            }
            isProviderAdded = true
            Log.d(TAG, "Mock providers registered successfully")
            return null
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Not set as mock location app", e)
            return "Vui lòng chọn 'GPS Mock' làm ứng dụng vị trí giả trong Cài đặt cho nhà phát triển (Developer Options)."
        } catch (e: Exception) {
            Log.e(TAG, "Exception initializing mock providers", e)
            return "Lỗi khởi tạo Mock Location: ${e.message}"
        }
    }

    /**
     * Publishes a mock location to the system test providers.
     */
    fun publishLocation(
        lat: Double,
        lng: Double,
        alt: Double = 10.0,
        speedMps: Float = 0f,
        bearingDeg: Float = 0f,
        accuracyMeters: Float = 2.0f
    ): Boolean {
        if (!isProviderAdded) {
            val err = setupMockProviders()
            if (err != null) return false
        }

        val currentTime = System.currentTimeMillis()
        val elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        for (provider in providers) {
            try {
                val loc = Location(provider).apply {
                    latitude = lat
                    longitude = lng
                    altitude = alt
                    time = currentTime
                    setElapsedRealtimeNanos(elapsedRealtimeNanos)
                    accuracy = accuracyMeters
                    speed = speedMps
                    bearing = bearingDeg

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        verticalAccuracyMeters = 1.0f
                        speedAccuracyMetersPerSecond = 0.5f
                        bearingAccuracyDegrees = 1.0f
                    }
                }
                locationManager.setTestProviderLocation(provider, loc)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting mock location for $provider", e)
                return false
            }
        }
        return true
    }

    /**
     * Removes mock test providers and resets location
     */
    fun cleanup() {
        for (provider in providers) {
            try {
                locationManager.setTestProviderEnabled(provider, false)
                locationManager.removeTestProvider(provider)
            } catch (e: Exception) {
                Log.w(TAG, "Cleanup provider error: ${e.message}")
            }
        }
        isProviderAdded = false
    }

    fun isMockAppAllowed(): Boolean {
        return try {
            locationManager.addTestProvider(
                "test_check",
                false, false, false, false, false, false, false,
                Criteria.POWER_LOW, Criteria.ACCURACY_COARSE
            )
            locationManager.removeTestProvider("test_check")
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
