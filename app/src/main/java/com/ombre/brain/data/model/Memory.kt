package com.ombre.brain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ombre.brain.data.database.Converters
import kotlinx.serialization.Serializable

/**
 * 记忆主实体 —— Ombre Brain 的核心数据
 *
 * 每一条记忆代表一次交互经历，像人脑中的一个"事件片断"。
 *
 * 记忆有三级状态：
 * - 活跃 (isActive = true, isHabit = false): 正常可召回的记忆
 * - 沉睡 (isActive = false, isHabit = false): 已衰减沉入潜意识层
 * - 固化 (isHabit = true): 永不遗忘的习惯/本能
 */
@Entity(tableName = "memories")
@TypeConverters(Converters::class)
@Serializable
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 记忆标题 —— 一句话概括 */
    val title: String,

    /** 记忆全文内容 */
    val content: String,

    /** 重要度 0.0 ~ 1.0 */
    val importance: Double = 0.3,

    /** 情感强度 -1.0(负面) ~ 1.0(正面)，0 为中性 */
    val sentiment: Double = 0.0,

    /** 标签列表，用于快速分类和关联 */
    val tags: List<String> = emptyList(),

    /** 创建时间 (Unix ms) */
    val createdAt: Long = System.currentTimeMillis(),

    /** 最后触发/回忆时间 (Unix ms) */
    val lastTriggeredAt: Long = System.currentTimeMillis(),

    /** 触发/回忆次数 */
    val triggerCount: Int = 1,

    /** 是否处于活跃池 */
    val isActive: Boolean = true,

    /** 是否已固化为习惯（永不遗忘） */
    val isHabit: Boolean = false,

    /** 是否已同步到 Operit 记忆 */
    val syncedToOperit: Boolean = false,

    /** 记忆来源：ai / user_interaction / system */
    val source: String = "ai",

    /** 关联的记忆 ID 列表 */
    val relatedMemoryIds: List<Long> = emptyList()
)
