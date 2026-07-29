package com.ombre.brain.data.database.dao

import androidx.room.*
import com.ombre.brain.data.model.FlashIdea
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashDao {

    @Query("SELECT * FROM flashes ORDER BY createdAt DESC")
    fun getAllFlashes(): Flow<List<FlashIdea>>

    @Query("SELECT * FROM flashes WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadFlashes(): Flow<List<FlashIdea>>

    @Query("SELECT * FROM flashes WHERE id = :id")
    suspend fun getFlashById(id: Long): FlashIdea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flash: FlashIdea): Long

    @Update
    suspend fun update(flash: FlashIdea)

    @Delete
    suspend fun delete(flash: FlashIdea)

    @Query("UPDATE flashes SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("SELECT COUNT(*) FROM flashes WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
}