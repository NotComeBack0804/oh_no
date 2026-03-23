package com.easyaccounting.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Security utilities using Android Keystore for safe key storage.
 * Avoids hardcoding encryption keys in source code.
 */
object SecurityUtils {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "EasyAccountingDBKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Get or create the database encryption key stored in Android Keystore.
     * The key never leaves the secure hardware and is not readable by the app.
     */
    fun getOrCreateDatabaseKey(context: Context): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }

        // If key doesn't exist, generate a new one
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }

        // Retrieve the key and use it to encrypt/decrypt a stored password
        return retrieveOrCreatePassword(context)
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Store the database password securely using Keystore.
     * The actual password is encrypted with the hardware-backed key.
     */
    private fun retrieveOrCreatePassword(context: Context): ByteArray {
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encryptedPassword = prefs.getString("encrypted_db_pass", null)

        return if (encryptedPassword != null) {
            decryptPassword(encryptedPassword)
        } else {
            // Generate a new random password and store it
            val newPassword = generateRandomPassword()
            val encrypted = encryptPassword(newPassword)
            prefs.edit().putString("encrypted_db_pass", encrypted).apply()
            newPassword.toByteArray()
        }
    }

    private fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val random = java.security.SecureRandom()
        return (1..32).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun encryptPassword(password: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        // Combine IV + encrypted data and encode as Base64
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    private fun decryptPassword(encryptedBase64: String): ByteArray {
        val combined = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)

        val iv = ByteArray(GCM_IV_LENGTH)
        val encrypted = ByteArray(combined.size - GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
        System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        return cipher.doFinal(encrypted)
    }
}
