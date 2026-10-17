package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.LocationMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GPS Mock", appName)
    }

    @Test
    fun `verify location math calculations`() {
        // Hanoi coordinates
        val lat1 = 21.028511
        val lon1 = 105.854167
        // Approx 1km away
        val lat2 = 21.037511
        val lon2 = 105.854167

        val distance = LocationMath.distanceMeters(lat1, lon1, lat2, lon2)
        assertTrue(distance > 900 && distance < 1100)

        val bearing = LocationMath.bearingDegrees(lat1, lon1, lat2, lon2)
        // Moving purely north should be near 0 degrees
        assertTrue(bearing < 5.0f || bearing > 355.0f)

        val (interLat, interLon) = LocationMath.interpolate(lat1, lon1, lat2, lon2, 0.5)
        assertEquals((lat1 + lat2) / 2.0, interLat, 0.0001)
        assertEquals((lon1 + lon2) / 2.0, interLon, 0.0001)
    }
}
