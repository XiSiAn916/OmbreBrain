package com.ombre.brain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ombre.brain.data.database.Converters
import kotlinx.serialization.Serializable

/**
 * 闪现想法 —— AI "触景生情"时自动生成的灵感片段
 *
 * 类似人类的"诶，我突然想到……"
 * 不需要用户主动触发，由 AssociationEngine 在上下文匹配时自动生成。
 */
@Entity(tableName = "flashes")
@TypeConverters(Converters::class)
@Serializable
data class FlashIdea(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 闪现想法的标题 */
    val title: String,

    /** 想法内容 */
    val content: String,

    /** 触发该想法的上下文描述 */
    val triggerContext: String,

    /** 关联的记忆 ID */
    val sourceMemoryId: Long,

    /** 关联度 0.0 ~ 1.0 */
    val relevance: Double,

    /** 创建时间 (Unix ms) */
    val createdAt: Long = System.currentTimeMillis(),

    /** 是否已被用户看过 */
    val isRead: Boolean = false,

    /** 标签 */
    val tags: List<String> = emptyList()
)
