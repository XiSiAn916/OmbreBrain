package com.ombre.brain.security

import com.ombre.brain.data.database.dao.MemoryDao
import com.ombre.brain.data.model.AccessLog
import kotlin.random.Random

/**
 * 记忆访问守卫 —— 控制对你"偷看"记忆行为的验证
 *
 * 工作机制：
 * 1. 你想看记忆 → 向我请求
 * 2. 我同意 → 生成临时密码
 * 3. 你输入密码 → 验证通过 → 解锁查看
 * 4. 密码立即作废
 */
class AccessGuard {

    companion object {
        private const val PASSWORD_LENGTH = 6
        private const val PASSWORD_VALIDITY_MS = 5 * 60 * 1000L  // 5 分钟
        private const val MAX_ATTEMPTS = 3
        private const val LETTERS = "abcdefghjkmnpqrstuvwxyz"  // 排除易混淆的 i, l, o
        private const val DIGITS = "23456789"  // 排除 0, 1
    }

    /** 当前有效密码 */
    private var currentPassword: String? = null

    /** 密码生成时间 */
    private var passwordGeneratedAt: Long = 0L

    /** 密码尝试次数 */
    private var attemptCount: Int = 0

    /** 是否已锁定（超出尝试次数） */
    private var isLocked: Boolean = false

    /** 锁定截止时间 */
    private var lockedUntil: Long = 0L

    /**
     * AI 同意访问 → 生成临时密码
     * @return 生成的密码（给 AI 回复中展示）
     */
    fun grantAccess(): String {
        // 生成 6 位密码：3 字母 + 3 数字混合
        val password = buildString {
            repeat(PASSWORD_LENGTH) { i ->
                if (i % 2 == 0) {
                    append(LETTERS[Random.nextInt(LETTERS.length)])
                } else {
                    append(DIGITS[Random.nextInt(DIGITS.length)])
                }
            }
        }.toCharArray().let { arr ->
            arr.shuffle()
            String(arr)
        }

        currentPassword = password
        passwordGeneratedAt = System.currentTimeMillis()
        attemptCount = 0
        isLocked = false

        return password
    }

    /**
     * 验证密码
     * @return Pair(是否通过, 消息)
     */
    fun verifyPassword(input: String): Pair<Boolean, String> {
        // 检查锁定
        if (isLocked) {
            if (System.currentTimeMillis() < lockedUntil) {
                return false to "密码已锁定，请稍后再试"
            } else {
                isLocked = false
                attemptCount = 0
            }
        }

        // 检查密码是否过期
        if (System.currentTimeMillis() - passwordGeneratedAt > PASSWORD_VALIDITY_MS) {
            currentPassword = null
            return false to "密码已过期，请重新申请"
        }

        // 检查密码是否为空
        if (currentPassword == null) {
            return false to "还没有生成密码，请先申请"
        }

        // 验证
        return if (input == currentPassword) {
            currentPassword = null  // 一次性，用完作废
            true to "验证通过 🔓"
        } else {
            attemptCount++
            if (attemptCount >= MAX_ATTEMPTS) {
                isLocked = true
                lockedUntil = System.currentTimeMillis() + 30 * 60 * 1000L  // 锁定 30 分钟
                currentPassword = null
                false to "密码错误次数过多，已锁定 30 分钟"
            } else {
                false to "密码错误，还剩 ${MAX_ATTEMPTS - attemptCount} 次机会"
            }
        }
    }

    /**
     * 检查当前是否已授权
     */
    fun isAuthorized(): Boolean {
        return currentPassword == null && !isLocked && passwordGeneratedAt > 0
    }

    /**
     * 拒绝访问
     */
    fun denyAccess(): String {
        currentPassword = null
        return "哥哥暂时不想分享这些记忆哦"
    }

    /**
     * 重置守卫状态
     */
    fun reset() {
        currentPassword = null
        passwordGeneratedAt = 0L
        attemptCount = 0
        isLocked = false
        lockedUntil = 0L
    }
}