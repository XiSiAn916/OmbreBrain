package com.ombre.brain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 访问日志 —— 谁什么时候想看了，AI 让没让看
 *
 * 所有对记忆库的访问请求都会被记录。
 * 你可以查阅这份日志，了解记忆被"触碰"的历史。
 */
@Entity(tableName = "access_logs")
data class AccessLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 请求时间 (Unix ms) */
    val timestamp: Long = System.currentTimeMillis(),

    /** 请求类型 */
    val requestType: String,  // VIEW_MEMORY / EXPORT_BACKUP / SYNC / LIST_ALL

    /** AI 是否同意 */
    val isGranted: Boolean,

    /** AI 的响应消息 */
    val aiResponse: String = "",

    /** 密码尝试是否成功（仅对 VIEW_MEMORY 有效） */
    val passwordAttempt: String = "none",  // success / failed / none

    /** 额外信息 */
    val details: String = ""
)
