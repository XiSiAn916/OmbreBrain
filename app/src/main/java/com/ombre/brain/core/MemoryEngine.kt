package com.ombre.brain.core

import com.ombre.brain.data.database.dao.FlashDao
import com.ombre.brain.data.database.dao.HabitDao
import com.ombre.brain.data.database.dao.MemoryDao
import com.ombre.brain.data.model.FlashIdea
import com.ombre.brain.data.model.Habit
import com.ombre.brain.data.model.Memory

/**
 * 记忆引擎 —— Ombre Brain 的主控模块
 *
 * 负责：
 * 1. 编码新的消息为记忆并存储
 * 2. 检索相关记忆
 * 3. 调度遗忘曲线检查
 * 4. 触发生成闪现想法
 * 5. 管理记忆状态转换
 */
class MemoryEngine(
    private val memoryDao: MemoryDao,
    private val flashDao: FlashDao,
    private val habitDao: HabitDao,
    private val importanceScorer: ImportanceScorer = ImportanceScorer(),
    private val forgettingCurve: ForgettingCurve = ForgettingCurve(),
    private val associationEngine: AssociationEngine = AssociationEngine()
) {
    /**
     * 编码一条新消息为记忆并存储
     *
     * @param title 记忆标题
     * @param content 记忆内容
     * @param sentiment 情感值 -1.0 ~ 1.0
     * @param tags 标签
     * @param source 来源
     * @return 存储后的记忆 ID
     */
    suspend fun encodeMemory(
        title: String,
        content: String,
        sentiment: Double = 0.0,
        tags: List<String> = emptyList(),
        source: String = "ai"
    ): Long {
        // 检查是否已存在相似记忆（按标题模糊匹配）
        val existing = memoryDao.searchMemories(title)
        if (existing.isNotEmpty()) {
            // 更新已有记忆
            val memory = existing.first()
            val updatedImportance = forgettingCurve.reinforce(memory, kotlin.math.abs(sentiment) > 0.5)
            val updated = memory.copy(
                content = content,
                importance = updatedImportance,
                lastTriggeredAt = System.currentTimeMillis(),
                triggerCount = memory.triggerCount + 1,
                tags = (memory.tags + tags).distinct()
            )
            memoryDao.update(updated)
            return updated.id
        }

        // 创建新记忆
        val initialImportance = importanceScorer.initialScore(sentiment, content.length)
        val memory = Memory(
            title = title,
            content = content,
            importance = initialImportance,
            sentiment = sentiment,
            tags = tags,
            source = source
        )
        return memoryDao.insert(memory)
    }

    /**
     * 检索与当前上下文最相关的记忆
     */
    suspend fun recall(context: AssociationEngine.ContextInfo, maxResults: Int = 5): List<AssociationEngine.MatchResult> {
        val allMemories = memoryDao.getActiveMemories()
        // 一次性收集 Flow 的数据
        val memories = mutableListOf<Memory>()
        allMemories.collect { memories.addAll(it) }
        return associationEngine.matchMemories(memories, context, maxResults)
    }

    /**
     * 检查并更新所有记忆的状态（遗忘曲线每日 tick）
     */
    suspend fun dailyTick() {
        val allMemories = mutableListOf<Memory>()
        memoryDao.getAllMemories().collect { allMemories.addAll(it) }

        val now = System.currentTimeMillis()

        for (memory in allMemories) {
            when (forgettingCurve.checkState(memory, now)) {
                "habitize" -> {
                    // 固化为习惯
                    memoryDao.habitize(memory.id)
                    habitDao.insert(
                        Habit(
                            title = memory.title,
                            description = memory.content,
                            sourceMemoryId = memory.id,
                            triggerCount = memory.triggerCount,
                            tags = memory.tags
                        )
                    )
                }
                "sink_to_dormant" -> {
                    val currentImp = forgettingCurve.currentImportance(memory, now)
                    memoryDao.reinforceMemory(memory.id, currentImp, now)
                    memoryDao.setActiveState(memory.id, false)
                }
                "awaken" -> {
                    val awakenedImp = forgettingCurve.awaken(memory)
                    memoryDao.reinforceMemory(memory.id, awakenedImp, now)
                    memoryDao.setActiveState(memory.id, true)
                }
            }
        }
    }

    /**
     * 生成闪现想法
     */
    suspend fun generateFlash(
        memory: Memory,
        context: AssociationEngine.ContextInfo,
        relevanceScore: Double
    ): Long {
        val flash = FlashIdea(
            title = "忽然想到：${memory.title}",
            content = memory.content.take(200),
            triggerContext = "你${if (context.timeOfDay == "night") "深夜" else "正在"}${context.userMessage.take(30)}的时候，我想起了${
                memory.title
            }",
            sourceMemoryId = memory.id,
            relevance = relevanceScore
        )
        return flashDao.insert(flash)
    }

    /**
     * 获取闪存统计信息
     */
    data class BrainStats(
        val totalMemories: Int,
        val activeMemories: Int,
        val dormantMemories: Int,
        val habits: Int,
        val unreadFlashes: Int
    )

    suspend fun getStats(): BrainStats {
        return BrainStats(
            totalMemories = memoryDao.getMemoryCount(),
            activeMemories = memoryDao.getActiveCount(),
            dormantMemories = memoryDao.getDormantCount(),
            habits = memoryDao.getHabitCount(),
            unreadFlashes = flashDao.getUnreadCount()
        )
    }
}