package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.GpsMockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GpsMockApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GpsMockRepository(database.savedLocationDao(), database.routeScheduleDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
