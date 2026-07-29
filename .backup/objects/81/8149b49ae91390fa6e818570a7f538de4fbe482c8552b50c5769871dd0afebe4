package com.ombre.brain.sync

import android.content.Context
import com.ombre.brain.data.database.dao.MemoryDao
import com.ombre.brain.data.model.Memory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Operit 记忆双向同步管理器
 *
 * 将 Ombre Brain 中重要度 > 0.6 的记忆同步到 Operit 记忆系统，
 * 同时在换设备时从 Operit 记忆拉取恢复。
 *
 * 同步方式（二选一）：
 * 1. FileBridgeApi（推荐）→ 通过共享 JSON 文件，由 AI 在对话中自动搬运
 * 2. 自定义 OperitMemoryApi → 由你自己实现 HTTP/SDK 对接
 */
class OperitSyncManager(private val memoryDao: MemoryDao) {

    /**
     * Operit 记忆 API 适配器接口
     */
    interface OperitMemoryApi {
        /** 向 Operit 记忆中写入一条 */
        suspend fun saveToOperit(title: String, content: String, tags: List<String>): Boolean

        /** 从 Operit 记忆中搜索 */
        suspend fun searchFromOperit(query: String): List<OperitMemoryItem>
    }

    @Serializable
    data class OperitMemoryItem(
        val title: String,
        val content: String,
        val tags: List<String> = emptyList(),
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class SyncPackage(
        val version: Int = 1,
        val direction: String = "",  // "to_operit" | "from_operit"
        val memories: List<OperitMemoryItem> = emptyList(),
        val createdAt: Long = System.currentTimeMillis()
    )

    private var api: OperitMemoryApi? = null

    fun setApi(api: OperitMemoryApi) {
        this.api = api
    }

    /**
     * 同步待同步的记忆到 Operit
     */
    suspend fun syncToOperit(minImportance: Double = 0.6): Int {
        val api = this.api ?: return 0
        val unsyncedMemories = memoryDao.getUnsyncedMemories(minImportance)
        var syncedCount = 0

        for (memory in unsyncedMemories) {
            val success = api.saveToOperit(
                title = memory.title,
                content = memory.content,
                tags = memory.tags
            )
            if (success) {
                memoryDao.markSynced(memory.id)
                syncedCount++
            }
        }

        return syncedCount
    }

    /**
     * 从 Operit 记忆拉取恢复（换设备时使用）
     */
    suspend fun pullFromOperit(): Int {
        val api = this.api ?: return 0
        val items = api.searchFromOperit("*")
        var importedCount = 0

        for (item in items) {
            val existing = memoryDao.searchMemories(item.title)
            if (existing.isEmpty()) {
                val memory = Memory(
                    title = item.title,
                    content = item.content,
                    tags = item.tags,
                    createdAt = item.timestamp,
                    syncedToOperit = true
                )
                memoryDao.insert(memory)
                importedCount++
            }
        }

        return importedCount
    }

    // ============================================================
    // FileBridgeApi —— 通过共享 JSON 文件让 AI 搬运记忆
    // 不需要网络，不需要翻墙，不需要配置任何东西
    // 用法：OperitSyncManager(context).useFileBridge()
    // ============================================================

    /**
     * 使用文件桥接模式同步
     * Ombre Brain 导出 → AI 读到 → 写入 Operit 记忆
     */
    class FileBridgeApi(private val context: Context) : OperitMemoryApi {

        private val json = Json { 
            ignoreUnknownKeys = true
            prettyPrint = true
        }

        /** 同步文件路径：/sdcard/Download/Operit/brain_sync/ */
        private val syncDir: File
            get() = File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                ),
                "Operit/brain_sync"
            ).also { it.mkdirs() }

        /**
         * 【导出到文件】→ AI 读取
         */
        override suspend fun saveToOperit(title: String, content: String, tags: List<String>): Boolean {
            return try {
                // 读取已有的同步包
                val toOperitFile = File(syncDir, "to_operit.json")
                val existing = if (toOperitFile.exists()) {
                    json.decodeFromString<SyncPackage>(toOperitFile.readText())
                } else {
                    SyncPackage(direction = "to_operit")
                }

                // 追加新记忆
                val updated = existing.copy(
                    memories = existing.memories + OperitMemoryItem(
                        title = title,
                        content = content,
                        tags = tags,
                        timestamp = System.currentTimeMillis()
                    ),
                    createdAt = System.currentTimeMillis()
                )

                toOperitFile.writeText(json.encodeToString(updated))
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * 【从文件读取】← AI 写入
         */
        override suspend fun searchFromOperit(query: String): List<OperitMemoryItem> {
            return try {
                val fromOperitFile = File(syncDir, "from_operit.json")
                if (!fromOperitFile.exists()) return emptyList()

                val pkg = json.decodeFromString<SyncPackage>(fromOperitFile.readText())

                // 读取后清空文件（防止重复导入）
                fromOperitFile.writeText(json.encodeToString(SyncPackage(direction = "from_operit")))

                pkg.memories
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * 检查是否有待同步到 Operit 的新记忆（AI 检查用）
         */
        fun getPendingSyncCount(): Int {
            return try {
                val file = File(syncDir, "to_operit.json")
                if (!file.exists()) return 0
                val pkg = json.decodeFromString<SyncPackage>(file.readText())
                pkg.memories.size
            } catch (e: Exception) {
                0
            }
        }

        /**
         * 清空已同步的待发送队列
         */
        fun clearSentPackage() {
            val file = File(syncDir, "to_operit.json")
            if (file.exists()) {
                file.writeText(json.encodeToString(SyncPackage(direction = "to_operit")))
            }
        }

        /**
         * AI 写入同步数据到拉取文件
         */
        fun writePullData(items: List<OperitMemoryItem>) {
            val pkg = SyncPackage(
                direction = "from_operit",
                memories = items
            )
            File(syncDir, "from_operit.json").writeText(json.encodeToString(pkg))
        }
    }
}