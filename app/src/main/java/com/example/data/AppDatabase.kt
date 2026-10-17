package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.RouteScheduleDao
import com.example.data.dao.SavedLocationDao
import com.example.data.model.RouteSchedule
import com.example.data.model.SavedLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SavedLocation::class, RouteSchedule::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun routeScheduleDao(): RouteScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gps_mock_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate presets on first run
                        scope.launch(Dispatchers.IO) {
                            val database = getDatabase(context, scope)
                            database.savedLocationDao().insertAll(PresetLocations.presets)
                            for (route in PresetLocations.sampleRoutes) {
                                database.routeScheduleDao().insert(route)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
