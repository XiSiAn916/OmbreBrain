package com.ombre.brain.data.database.dao

import androidx.room.*
import com.ombre.brain.data.model.Memory
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories ORDER BY importance DESC, lastTriggeredAt DESC")
    fun getAllMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE isActive = 1 ORDER BY importance DESC, lastTriggeredAt DESC")
    fun getActiveMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE isHabit = 1 ORDER BY importance DESC")
    fun getHabits(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE isActive = 0 AND isHabit = 0 ORDER BY lastTriggeredAt ASC")
    fun getDormantMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): Memory?

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<Memory>

    @Query("SELECT * FROM memories WHERE syncedToOperit = 0 AND importance >= :minImportance")
    suspend fun getUnsyncedMemories(minImportance: Double = 0.6): List<Memory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: Memory): Long

    @Update
    suspend fun update(memory: Memory)

    @Delete
    suspend fun delete(memory: Memory)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE memories SET importance = :importance, lastTriggeredAt = :now, triggerCount = triggerCount + 1 WHERE id = :id")
    suspend fun reinforceMemory(id: Long, importance: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE memories SET isActive = :isActive WHERE id = :id")
    suspend fun setActiveState(id: Long, isActive: Boolean)

    @Query("UPDATE memories SET isHabit = 1, importance = 1.0 WHERE id = :id")
    suspend fun habitize(id: Long)

    @Query("UPDATE memories SET syncedToOperit = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun getMemoryCount(): Int

    @Query("SELECT COUNT(*) FROM memories WHERE isActive = 1 AND isHabit = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM memories WHERE isActive = 0 AND isHabit = 0")
    suspend fun getDormantCount(): Int

    @Query("SELECT COUNT(*) FROM memories WHERE isHabit = 1")
    suspend fun getHabitCount(): Int
}