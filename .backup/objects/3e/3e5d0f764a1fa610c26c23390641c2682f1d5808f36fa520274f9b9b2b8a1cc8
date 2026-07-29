package com.ombre.brain.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 加密工具 —— AES-256-GCM 加密
 *
 * 使用 Android KeyStore 存储密钥（硬件级安全，不可导出）。
 * 每条记忆在写入 SQLite 之前先加密，读取时解密。
 */
object CryptoUtil {

    private const val KEYSTORE_ALIAS = "ombre_brain_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128  // bits

    /**
     * 获取或创建密钥
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // 检查是否已存在
        keyStore.getEntry(KEYSTORE_ALIAS, null)?.let { entry ->
            return (entry as KeyStore.SecretKeyEntry).secretKey
        }

        // 创建新密钥
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * 加密字符串
     * @return 格式: Base64(IV)::Base64(密文)
     */
    fun encrypt(plainText: String): String {
        val secretKey = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return "${android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)}::${
            android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
        }"
    }

    /**
     * 解密字符串
     */
    fun decrypt(encryptedText: String): String {
        val parts = encryptedText.split("::")
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted text format")

        val iv = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
        val encrypted = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)

        val secretKey = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }
}