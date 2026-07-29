package com.ombre.brain.sync

import android.content.Context
import com.ombre.brain.data.database.OmbreDatabase
import com.ombre.brain.data.model.FlashIdea
import com.ombre.brain.data.model.Habit
import com.ombre.brain.data.model.Memory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 备份管理器 —— 导出/导入 .ombre 加密备份文件
 *
 * 备份文件格式：
 *   [魔数 8B] [版本 2B] [时间戳 8B] [加密数据区] [MAC 16B]
 */
class BackupManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Serializable
    data class BackupData(
        val version: Int = 1,
        val createdAt: Long = System.currentTimeMillis(),
        val memories: List<Memory>,
        val flashes: List<FlashIdea>,
        val habits: List<Habit>
    )

    companion object {
        private const val MAGIC = "OMBRE_BRAIN"
        private const val BACKUP_VERSION = 1
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * 导出备份
     * @param password 用户设定的备份密码
     * @return 备份文件路径
     */
    suspend fun export(password: String): String {
        val db = OmbreDatabase.getDatabase(context)
        val memories = mutableListOf<Memory>()
        val flashes = mutableListOf<FlashIdea>()
        val habits = mutableListOf<Habit>()

        db.memoryDao().getAllMemories().collect { memories.addAll(it) }
        db.flashDao().getAllFlashes().collect { flashes.addAll(it) }
        db.habitDao().getAllHabits().collect { habits.addAll(it) }

        val backupData = BackupData(
            version = BACKUP_VERSION,
            memories = memories,
            flashes = flashes,
            habits = habits
        )

        val jsonString = json.encodeToString(backupData)

        // 生成密钥
        val key = deriveKey(password)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(jsonString.toByteArray(Charsets.UTF_8))
        val gcmTag = encrypted.copyOfRange(encrypted.size - 16, encrypted.size)

        // 写入文件
        val backupDir = File(context.getExternalFilesDir(null), "OmbreBrain/backups")
        backupDir.mkdirs()
        val backupFile = File(backupDir, "ombre-brain-backup_${System.currentTimeMillis()}.ombre")

        FileOutputStream(backupFile).use { fos ->
            // 魔数
            fos.write(MAGIC.toByteArray())
            // 版本
            fos.write(byteArrayOf(0, BACKUP_VERSION.toByte()))
            // 时间戳
            val timestamp = ByteArray(8)
            var ts = System.currentTimeMillis()
            for (i in 7 downTo 0) {
                timestamp[i] = (ts and 0xFF).toByte()
                ts = ts shr 8
            }
            fos.write(timestamp)
            // IV
            fos.write(iv)
            // 密文
            fos.write(encrypted)
            // MAC
            val mac = MessageDigest.getInstance("SHA-256")
                .digest(encrypted + password.toByteArray())
            fos.write(mac.take(16).toByteArray())
        }

        // 同时复制到公共下载目录以便传输
        val publicFile = File(context.getExternalFilesDir(null), "Download/ombre-backup.ombre")
        backupFile.copyTo(publicFile, overwrite = true)

        return backupFile.absolutePath
    }

    /**
     * 导入备份
     * @param filePath .ombre 文件路径
     * @param password 备份密码
     */
    suspend fun import(filePath: String, password: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false

        val fileBytes = file.readBytes()
        var offset = 0

        // 验证魔数
        val magic = String(fileBytes.sliceArray(offset until offset + MAGIC.length))
        if (magic != MAGIC) return false
        offset += MAGIC.length

        // 跳过版本和时间戳
        offset += 10

        // 读取 IV
        val iv = fileBytes.sliceArray(offset until offset + 12)
        offset += 12

        // 读取密文（到结尾前16字节的MAC）
        val macStart = fileBytes.size - 16
        val encrypted = fileBytes.sliceArray(offset until macStart)
        val storedMac = fileBytes.sliceArray(macStart until fileBytes.size)

        // 验证 MAC
        val computedMac = MessageDigest.getInstance("SHA-256")
            .digest(encrypted + password.toByteArray())
            .take(16).toByteArray()
        if (!storedMac.contentEquals(computedMac)) {
            throw SecurityException("密码错误或文件已损坏")
        }

        // 解密
        val key = deriveKey(password)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decrypted = cipher.doFinal(encrypted)
        val jsonString = String(decrypted, Charsets.UTF_8)

        val backupData = json.decodeFromString<BackupData>(jsonString)

        // 写入数据库
        val db = OmbreDatabase.getDatabase(context)
        for (memory in backupData.memories) {
            db.memoryDao().insert(memory)
        }
        for (flash in backupData.flashes) {
            db.flashDao().insert(flash)
        }
        for (habit in backupData.habits) {
            db.habitDao().insert(habit)
        }

        return true
    }

    private fun deriveKey(password: String): SecretKeySpec {
        val salt = "OmbreBrainSalt".toByteArray()
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}