package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MockHistoryDao
import com.example.data.dao.RouteScheduleDao
import com.example.data.dao.SavedLocationDao
import com.example.data.model.MockHistoryEntry
import com.example.data.model.RouteSchedule
import com.example.data.model.SavedLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SavedLocation::class, RouteSchedule::class, MockHistoryEntry::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun routeScheduleDao(): RouteScheduleDao
    abstract fun mockHistoryDao(): MockHistoryDao

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
                        scope.launch(Dispatchers.IO) {
                            val database = getDatabase(context, scope)
                            database.savedLocationDao().insertAll(PresetLocations.presets)
                            for (route in PresetLocations.sampleRoutes) {
                                database.routeScheduleDao().insert(route)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
