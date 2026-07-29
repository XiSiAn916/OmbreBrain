package com.ombre.brain.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ombre.brain.data.database.dao.FlashDao
import com.ombre.brain.data.database.dao.HabitDao
import com.ombre.brain.data.database.dao.MemoryDao
import com.ombre.brain.data.model.AccessLog
import com.ombre.brain.data.model.FlashIdea
import com.ombre.brain.data.model.Habit
import com.ombre.brain.data.model.Memory

@Database(
    entities = [Memory::class, FlashIdea::class, Habit::class, AccessLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OmbreDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao
    abstract fun flashDao(): FlashDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: OmbreDatabase? = null

        fun getDatabase(context: Context): OmbreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmbreDatabase::class.java,
                    "ombre_brain.db"  // 数据库文件
                )
                    .fallbackToDestructiveMigration()  // 开发期方便，发布前换成 migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}