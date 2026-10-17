package com.example.service

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object LocationMath {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance in meters between two lat/lng points using Haversine formula
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates initial bearing in degrees (0..360) from point 1 to point 2
     */
    fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val lambdaDiff = Math.toRadians(lon2 - lon1)

        val y = sin(lambdaDiff) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(lambdaDiff)
        val theta = atan2(y, x)
        val bearing = Math.toDegrees(theta)
        return ((bearing + 360.0) % 360.0).toFloat()
    }

    /**
     * Moves from a start point along a bearing by distance in meters
     */
    fun moveByBearing(lat: Double, lon: Double, bearingDeg: Float, distanceMeters: Double): Pair<Double, Double> {
        val distRatio = distanceMeters / EARTH_RADIUS_METERS
        val bearingRad = Math.toRadians(bearingDeg.toDouble())

        val lat1Rad = Math.toRadians(lat)
        val lon1Rad = Math.toRadians(lon)

        val lat2Rad = kotlin.math.asin(
            sin(lat1Rad) * cos(distRatio) + cos(lat1Rad) * sin(distRatio) * cos(bearingRad)
        )
        val lon2Rad = lon1Rad + atan2(
            sin(bearingRad) * sin(distRatio) * cos(lat1Rad),
            cos(distRatio) - sin(lat1Rad) * sin(lat2Rad)
        )

        return Pair(Math.toDegrees(lat2Rad), Math.toDegrees(lon2Rad))
    }

    /**
     * Interpolates linearly between two coordinates given fraction [0..1]
     */
    fun interpolate(lat1: Double, lon1: Double, lat2: Double, lon2: Double, fraction: Double): Pair<Double, Double> {
        val clamped = fraction.coerceIn(0.0, 1.0)
        val targetLat = lat1 + (lat2 - lat1) * clamped
        val targetLon = lon1 + (lon2 - lon1) * clamped
        return Pair(targetLat, targetLon)
    }

    /**
     * Optional micro-jitter for realistic satellite noise (~0.5 - 1.5m jitter)
     */
    fun applyJitter(lat: Double, lon: Double, enabled: Boolean): Pair<Double, Double> {
        if (!enabled) return Pair(lat, lon)
        // ~0.000005 deg is approx 0.5 meter
        val latJitter = (Random.nextDouble() - 0.5) * 0.000008
        val lonJitter = (Random.nextDouble() - 0.5) * 0.000008
        return Pair(lat + latJitter, lon + lonJitter)
    }
}
