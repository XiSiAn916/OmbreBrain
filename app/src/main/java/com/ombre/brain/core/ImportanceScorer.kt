package com.ombre.brain.core

import com.ombre.brain.data.model.Memory

/**
 * 重要度评分器
 *
 * 综合多个维度计算一条记忆的重要度 (0.0 ~ 1.0)
 *
 * 权重分配：
 * - 频次权重 20%：被提及/触发的次数
 * - 情感权重 30%：情感强度绝对值
 * - 交互深度 30%：用户参与度（消息长度、交互轮次等）
 * - 用户引用 20%：用户主动提及/引用该记忆
 */
class ImportanceScorer {

    companion object {
        private const val FREQUENCY_WEIGHT = 0.20
        private const val SENTIMENT_WEIGHT = 0.30
        private const val DEPTH_WEIGHT = 0.30
        private const val REFERENCE_WEIGHT = 0.20

        // 频次饱和点（超过此值不再增加频次得分）
        private const val FREQUENCY_SATURATION = 50.0
        // 频次缩放因子
        private const val FREQUENCY_SCALE = 20.0
    }

    /**
     * 计算重要度
     */
    fun calculate(memory: Memory, userReferenced: Boolean = false): Double {
        val frequencyScore = calculateFrequencyScore(memory.triggerCount)
        val sentimentScore = calculateSentimentScore(memory.sentiment)
        val depthScore = calculateDepthScore(memory)
        val referenceScore = if (userReferenced) 1.0 else 0.0

        return (frequencyScore * FREQUENCY_WEIGHT +
                sentimentScore * SENTIMENT_WEIGHT +
                depthScore * DEPTH_WEIGHT +
                referenceScore * REFERENCE_WEIGHT)
            .coerceIn(0.0, 1.0)
    }

    /**
     * 快速计算新记忆的初始重要度
     */
    fun initialScore(sentiment: Double, contentLength: Int): Double {
        val sentimentContribution = kotlin.math.abs(sentiment) * 0.4
        val lengthContribution = (contentLength.toDouble() / 500.0).coerceIn(0.0, 0.3)
        return (0.2 + sentimentContribution + lengthContribution).coerceIn(0.0, 1.0)
    }

    /**
     * 频次得分：对数增长，逐渐饱和
     */
    private fun calculateFrequencyScore(count: Int): Double {
        if (count <= 0) return 0.0
        return (kotlin.math.log(count.toDouble() + 1.0) /
                kotlin.math.log(FREQUENCY_SATURATION + 1.0))
            .coerceIn(0.0, 1.0)
    }

    /**
     * 情感得分：情感越强烈（无论正负），得分越高
     */
    private fun calculateSentimentScore(sentiment: Double): Double {
        return kotlin.math.abs(sentiment).coerceIn(0.0, 1.0)
    }

    /**
     * 深度得分：基于内容复杂度和关联记忆数
     */
    private fun calculateDepthScore(memory: Memory): Double {
        val contentScore = (memory.content.length.toDouble() / 1000.0).coerceIn(0.0, 0.5)
        val tagScore = (memory.tags.size.toDouble() / 10.0).coerceIn(0.0, 0.3)
        val relationScore = (memory.relatedMemoryIds.size.toDouble() / 20.0).coerceIn(0.0, 0.2)
        return (contentScore + tagScore + relationScore).coerceIn(0.0, 1.0)
    }
}