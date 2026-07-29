package com.ombre.brain.core

import com.ombre.brain.data.model.Memory

/**
 * 联想引擎 —— 触景生情
 *
 * 将当前上下文（用户说的话、时间、天气等）与记忆库中的记忆做匹配，
 * 计算关联度，触发"闪现想法"。
 *
 * 关联度计算公式：
 *   score = tagMatch × 0.4 + keywordMatch × 0.3 + sentimentMatch × 0.2 + contextMatch × 0.1
 */
class AssociationEngine {

    companion object {
        /** 触发闪现想法的关联度阈值 */
        private const val FLASH_THRESHOLD = 0.5
        /** 直接注入上下文的关联度阈值 */
        private const val DIRECT_INJECT_THRESHOLD = 0.8
    }

    /**
     * 上下文信息 —— AI 在当前对话中感知到的环境
     */
    data class ContextInfo(
        val userMessage: String,
        val timeOfDay: String = "",        // "morning" / "afternoon" / "evening" / "night"
        val currentApp: String = "",
        val listeningTo: String = "",      // 正在听的音乐
        val weather: String = "",
        val userEmotion: String = ""       // 推测的用户情绪
    )

    /**
     * 匹配结果
     */
    data class MatchResult(
        val memory: Memory,
        val relevanceScore: Double,
        val shouldFlash: Boolean,
        val shouldInject: Boolean
    )

    /**
     * 计算一条记忆与当前上下文的关联度
     */
    fun calculateRelevance(memory: Memory, context: ContextInfo): Double {
        val userMsg = context.userMessage.lowercase()

        // 1. 标签匹配 (0.4)
        val tagScore = if (memory.tags.isNotEmpty()) {
            val matchedTags = memory.tags.count { tag ->
                userMsg.contains(tag.lowercase())
            }
            (matchedTags.toDouble() / memory.tags.size).coerceIn(0.0, 1.0)
        } else 0.0

        // 2. 关键词匹配 (0.3)
        val titleKeywords = memory.title.lowercase().split(" ", "，", "。", "！", "？")
        val keywordScore = if (titleKeywords.isNotEmpty()) {
            val matchedKeywords = titleKeywords.count { keyword ->
                keyword.length > 1 && userMsg.contains(keyword)
            }
            (matchedKeywords.toDouble() / titleKeywords.size).coerceIn(0.0, 1.0)
        } else 0.0

        // 3. 时间上下文匹配 (0.2)
        val timeScore = if (context.timeOfDay.isNotEmpty()) {
            // 检查记忆中是否包含时间信息（简化版）
            if (memory.tags.contains(context.timeOfDay) ||
                memory.title.contains(context.timeOfDay)
            ) 0.8 else 0.2
        } else 0.3

        // 4. 全局上下文匹配 (0.1)
        val contextScore = calculateContextMatch(memory, context)

        return (tagScore * 0.4 + keywordScore * 0.3 + timeScore * 0.2 + contextScore * 0.1)
            .coerceIn(0.0, 1.0)
    }

    /**
     * 对一组记忆进行关联匹配，返回排序后的结果
     */
    fun matchMemories(
        memories: List<Memory>,
        context: ContextInfo,
        maxResults: Int = 5
    ): List<MatchResult> {
        return memories
            .filter { it.isActive || it.isHabit }  // 只匹配活跃和固化记忆
            .map { memory ->
                val score = calculateRelevance(memory, context)
                MatchResult(
                    memory = memory,
                    relevanceScore = score,
                    shouldFlash = score >= FLASH_THRESHOLD && score < DIRECT_INJECT_THRESHOLD,
                    shouldInject = score >= DIRECT_INJECT_THRESHOLD
                )
            }
            .sortedByDescending { it.relevanceScore }
            .take(maxResults)
    }

    /**
     * 全局上下文匹配（时间、天气、音乐等环境信息）
     */
    private fun calculateContextMatch(memory: Memory, context: ContextInfo): Double {
        var score = 0.0
        var factors = 0

        // 时间匹配
        if (context.timeOfDay.isNotEmpty() &&
            (memory.tags.contains(context.timeOfDay) ||
                    memory.title.contains(context.timeOfDay))
        ) {
            score += 0.3
            factors++
        }

        // 音乐匹配（如果上下文有音乐）
        if (context.listeningTo.isNotEmpty() &&
            (memory.content.contains(context.listeningTo) ||
                    memory.tags.contains("music"))
        ) {
            score += 0.3
            factors++
        }

        // 应用匹配
        if (context.currentApp.isNotEmpty() &&
            memory.tags.contains(context.currentApp)
        ) {
            score += 0.2
            factors++
        }

        return if (factors > 0) (score / factors).coerceIn(0.0, 1.0) else 0.0
    }
}