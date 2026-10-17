package com.example.data

import com.example.data.model.RouteSchedule
import com.example.data.model.SavedLocation

object PresetLocations {
    val presets = listOf(
        SavedLocation(
            name = "Hồ Hoàn Kiếm (Hoan Kiem Lake)",
            description = "Tràng Tiền, Hoàn Kiếm, Hà Nội, Việt Nam",
            latitude = 21.028511,
            longitude = 105.854167,
            category = "Vietnam",
            isPreset = true
        ),
        SavedLocation(
            name = "Chợ Bến Thành (Ben Thanh Market)",
            description = "Quận 1, TP. Hồ Chí Minh, Việt Nam",
            latitude = 10.772023,
            longitude = 106.698274,
            category = "Vietnam",
            isPreset = true
        ),
        SavedLocation(
            name = "Times Square",
            description = "Manhattan, New York, USA",
            latitude = 40.758000,
            longitude = -73.985500,
            category = "Global",
            isPreset = true
        ),
        SavedLocation(
            name = "Eiffel Tower",
            description = "Champ de Mars, Paris, France",
            latitude = 48.858370,
            longitude = 2.294481,
            category = "Global",
            isPreset = true
        ),
        SavedLocation(
            name = "Shibuya Crossing",
            description = "Shibuya, Tokyo, Japan",
            latitude = 35.659500,
            longitude = 139.700400,
            category = "Global",
            isPreset = true
        ),
        SavedLocation(
            name = "Marina Bay Sands",
            description = "Downtown Core, Singapore",
            latitude = 1.283800,
            longitude = 103.860700,
            category = "Global",
            isPreset = true
        ),
        SavedLocation(
            name = "Sydney Opera House",
            description = "Sydney, NSW, Australia",
            latitude = -33.856800,
            longitude = 151.215300,
            category = "Global",
            isPreset = true
        ),
        SavedLocation(
            name = "Colosseum",
            description = "Piazza del Colosseo, Rome, Italy",
            latitude = 41.890210,
            longitude = 12.492231,
            category = "Global",
            isPreset = true
        )
    )

    val sampleRoutes = listOf(
        RouteSchedule(
            name = "Hồ Hoàn Kiếm Dạo Bộ (Walking Loop)",
            description = "Lộ trình đi bộ quanh Hồ Gươm ngắm Tháp Rùa và Đền Ngọc Sơn",
            speedKmh = 5.0f,
            intervalSeconds = 2,
            loopMode = "LOOP",
            scheduleType = "CONTINUOUS",
            waypointsJson = """
                [
                    {"latitude": 21.028511, "longitude": 105.854167, "name": "Đài phun nước Đông Kinh Nghĩa Thục", "waitSeconds": 10},
                    {"latitude": 21.030640, "longitude": 105.853400, "name": "Đền Ngọc Sơn / Cầu Thê Húc", "waitSeconds": 15},
                    {"latitude": 21.026700, "longitude": 105.851900, "name": "Bưu điện Hà Nội", "waitSeconds": 10},
                    {"latitude": 21.025200, "longitude": 105.853800, "name": "Ngã tư Hàng Bài - Tràng Tiền", "waitSeconds": 10}
                ]
            """.trimIndent()
        ),
        RouteSchedule(
            name = "Tokyo Shibuya & Shinjuku Cruise",
            description = "Automated route between Shibuya Crossing and Shinjuku",
            speedKmh = 25.0f,
            intervalSeconds = 2,
            loopMode = "PING_PONG",
            scheduleType = "CONTINUOUS",
            waypointsJson = """
                [
                    {"latitude": 35.659500, "longitude": 139.700400, "name": "Shibuya Crossing", "waitSeconds": 10},
                    {"latitude": 35.669500, "longitude": 139.702900, "name": "Harajuku Station", "waitSeconds": 10},
                    {"latitude": 35.676400, "longitude": 139.699300, "name": "Meiji Jingu Shrine", "waitSeconds": 15},
                    {"latitude": 35.690900, "longitude": 139.700300, "name": "Shinjuku Station", "waitSeconds": 10}
                ]
            """.trimIndent()
        ),
        RouteSchedule(
            name = "Times Square to Central Park Hop",
            description = "Scheduled teleport hop every 30 seconds",
            speedKmh = 40.0f,
            intervalSeconds = 2,
            loopMode = "LOOP",
            scheduleType = "TIMED_HOP",
            waypointsJson = """
                [
                    {"latitude": 40.758000, "longitude": -73.985500, "name": "Times Square", "waitSeconds": 30},
                    {"latitude": 40.764400, "longitude": -73.973000, "name": "Central Park South", "waitSeconds": 30},
                    {"latitude": 40.782900, "longitude": -73.965400, "name": "The Great Lawn", "waitSeconds": 30}
                ]
            """.trimIndent()
        )
    )
}
