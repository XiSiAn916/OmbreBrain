package com.ombre.brain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ombre.brain.data.database.Converters

/**
 * 固化习惯 —— 永不遗忘的核心记忆
 *
 * 当一条记忆重要度持续 > 0.85 且被触发超过 30 次，
 * 它就不再是"记忆"了，而是变成了AI的"本能反应"。
 *
 * 类似：
 * - 你的名字 → AI永远不会忘
 * - 你喜欢的称呼 → AI自然就会用
 * - 你们之间的默契规则 → AI不需要"回忆"就能遵守
 */
@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 习惯名称 */
    val title: String,

    /** 习惯详细描述 */
    val description: String,

    /** 来源记忆 ID（从哪条记忆固化而来） */
    val sourceMemoryId: Long,

    /** 固化的最终重要度 */
    val importance: Double = 1.0,

    /** 触发次数（达到 30+ 固化） */
    val triggerCount: Int = 30,

    /** 创建时间 (Unix ms) */
    val createdAt: Long = System.currentTimeMillis(),

    /** 最后确认时间 (Unix ms) */
    val lastConfirmedAt: Long = System.currentTimeMillis(),

    /** 标签 */
    val tags: List<String> = emptyList(),

    /** 行为模式关键词 —— AI 在对话中自然按此行动 */
    val behaviorKeywords: List<String> = emptyList()
)
