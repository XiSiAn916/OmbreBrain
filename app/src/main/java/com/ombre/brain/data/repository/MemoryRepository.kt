package com.ombre.brain.data.repository

import com.ombre.brain.data.database.dao.FlashDao
import com.ombre.brain.data.database.dao.HabitDao
import com.ombre.brain.data.database.dao.MemoryDao
import com.ombre.brain.data.model.FlashIdea
import com.ombre.brain.data.model.Habit
import com.ombre.brain.data.model.Memory
import kotlinx.coroutines.flow.Flow

/**
 * 数据仓库 —— 统一访问所有记忆数据的入口
 */
class MemoryRepository(
    private val memoryDao: MemoryDao,
    private val flashDao: FlashDao,
    private val habitDao: HabitDao
) {
    // ---- 记忆 (Memories) ----

    val allMemories: Flow<List<Memory>> = memoryDao.getAllMemories()
    val activeMemories: Flow<List<Memory>> = memoryDao.getActiveMemories()
    val habits: Flow<List<Memory>> = memoryDao.getHabits()
    val dormantMemories: Flow<List<Memory>> = memoryDao.getDormantMemories()

    suspend fun getMemoryById(id: Long): Memory? = memoryDao.getMemoryById(id)
    suspend fun searchMemories(query: String): List<Memory> = memoryDao.searchMemories(query)
    suspend fun insertMemory(memory: Memory): Long = memoryDao.insert(memory)
    suspend fun updateMemory(memory: Memory) = memoryDao.update(memory)
    suspend fun deleteMemory(id: Long) = memoryDao.deleteById(id)

    // ---- 闪现想法 (Flashes) ----

    val allFlashes: Flow<List<FlashIdea>> = flashDao.getAllFlashes()
    val unreadFlashes: Flow<List<FlashIdea>> = flashDao.getUnreadFlashes()

    suspend fun getFlashById(id: Long): FlashIdea? = flashDao.getFlashById(id)
    suspend fun insertFlash(flash: FlashIdea): Long = flashDao.insert(flash)
    suspend fun markFlashRead(id: Long) = flashDao.markAsRead(id)

    // ---- 固化习惯 (Habits) ----

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)
    suspend fun deleteHabit(id: Long) = habitDao.deleteById(id)

    // ---- 统计 ----
    suspend fun getStats() = memoryDao.let {
        Triple(
            it.getMemoryCount(),
            it.getActiveCount(),
            it.getDormantCount()
        )
    }
}