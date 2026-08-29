package com.example.ayahflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ayahflow.data.model.AyahEntity
import com.example.ayahflow.data.model.BookmarkEntity
import com.example.ayahflow.data.model.ProgressEntity
import com.example.ayahflow.data.model.DailyHistoryEntity
import com.example.ayahflow.data.model.DailyReadLogEntity

@Database(
    entities = [AyahEntity::class, ProgressEntity::class, BookmarkEntity::class, DailyHistoryEntity::class, DailyReadLogEntity::class],
    version = 4,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun ayahDao(): AyahDao
    abstract fun progressDao(): ProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun dailyHistoryDao(): DailyHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `daily_history` (`date` TEXT NOT NULL, `ayahsRead` INTEGER NOT NULL, PRIMARY KEY(`date`))")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `daily_read_log` (`date` TEXT NOT NULL, `globalIndex` INTEGER NOT NULL, PRIMARY KEY(`date`, `globalIndex`))")
            }
        }

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "ayahflow_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
