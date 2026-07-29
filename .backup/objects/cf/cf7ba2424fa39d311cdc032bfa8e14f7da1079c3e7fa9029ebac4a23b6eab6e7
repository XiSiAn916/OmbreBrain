package com.ombre.brain.core

import com.ombre.brain.data.model.Memory
import kotlin.math.exp
import kotlin.math.log

/**
 * 遗忘曲线引擎 —— Ebbinghaus 变体
 *
 * 核心逻辑：
 *   S(t) = S₀ × e^(-λ × t) + ΔR
 *
 * 每次被回忆时强化，长期未被触发则衰减。
 * 重要度过低 → 沉入沉睡池；极高 + 高频 → 固化为习惯。
 */
class ForgettingCurve(
    /** 衰减系数（默认 0.05，越大忘得越快） */
    private val decayRate: Double = 0.05,
    /** 每次被回忆时的强化增量 */
    private val reinforceAmount: Double = 0.1,
    /** 活跃/沉睡分界线 */
    private val dormantThreshold: Double = 0.3,
    /** 固化最低重要度 */
    private val habitThreshold: Double = 0.85,
    /** 固化最少触发次数 */
    private val habitMinCount: Int = 30
) {

    /**
     * 计算当前重要度
     * @param memory 记忆对象
     * @param nowMs 当前时间 (Unix ms)
     * @return 当前应该的重要度
     */
    fun currentImportance(memory: Memory, nowMs: Long = System.currentTimeMillis()): Double {
        if (memory.isHabit) return 1.0  // 固化记忆永不衰减

        val daysSinceTrigger = (nowMs - memory.lastTriggeredAt) / (1000.0 * 60 * 60 * 24)
        if (daysSinceTrigger <= 0) return memory.importance

        // S(t) = S₀ × e^(-λ × t)
        val decayed = memory.importance * exp(-decayRate * daysSinceTrigger)
        return decayed.coerceIn(0.0, 1.0)
    }

    /**
     * 强化记忆（被回忆/触发时调用）
     * @return 强化后的重要度
     */
    fun reinforce(memory: Memory, emotionalContext: Boolean = false): Double {
        if (memory.isHabit) return 1.0

        val emotionalBonus = if (emotionalContext) reinforceAmount * 0.5 else 0.0
        val reinforced = memory.importance + reinforceAmount + emotionalBonus
        return reinforced.coerceIn(0.0, 1.0)
    }

    /**
     * 用户主动引用时的强化（大幅强化）
     */
    fun userReferenced(memory: Memory): Double {
        if (memory.isHabit) return 1.0
        val reinforced = memory.importance + 0.2  // 用户主动引用 +0.2
        return reinforced.coerceIn(0.0, 1.0)
    }

    /**
     * 检查记忆当前状态
     * @return 建议的操作: "active" / "dormant" / "habitize" / "none"
     */
    fun checkState(memory: Memory, nowMs: Long = System.currentTimeMillis()): String {
        if (memory.isHabit) return "none"

        val currentImp = currentImportance(memory, nowMs)

        // 检查固化条件
        if (currentImp >= habitThreshold && memory.triggerCount >= habitMinCount) {
            return "habitize"
        }

        // 检查是否应沉入沉睡池
        if (currentImp < dormantThreshold && !memory.isActive) {
            return "dormant"
        }

        // 如果当前重要度低于阈值但还在活跃池，建议下沉
        if (currentImp < dormantThreshold && memory.isActive) {
            return "sink_to_dormant"
        }

        // 如果沉睡了但重要度回来了，建议唤醒
        if (currentImp >= dormantThreshold && !memory.isActive) {
            return "awaken"
        }

        return "none"
    }

    /**
     * 唤醒沉睡记忆（关联触发时一次性增幅）
     */
    fun awaken(memory: Memory): Double {
        val awakened = memory.importance + 0.25
        return awakened.coerceIn(0.0, 1.0)
    }

    /**
     * 情感加成：强烈情感的记忆衰减减半
     */
    fun emotionalDecayRate(sentiment: Double): Double {
        return if (kotlin.math.abs(sentiment) > 0.6) {
            decayRate / 2  // 衰减速度减半
        } else {
            decayRate
        }
    }

    /**
     * 关联网络强化：当记忆A被强化时，关联记忆B也获得少量强化
     */
    fun relatedReinforce(relevanceScore: Double): Double {
        return reinforceAmount * relevanceScore * 0.5
    }
}