package com.easyaccounting.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecretStoreUtils {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "EasyAccountingSecretStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        ensureKey()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        val payload = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, payload, 0, iv.size)
        System.arraycopy(encrypted, 0, payload, iv.size, encrypted.size)
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(value: String): String {
        if (value.isBlank()) return ""
        ensureKey()

        val payload = Base64.decode(value, Base64.NO_WRAP)
        if (payload.size <= GCM_IV_LENGTH) return ""

        val iv = ByteArray(GCM_IV_LENGTH)
        val encrypted = ByteArray(payload.size - GCM_IV_LENGTH)
        System.arraycopy(payload, 0, iv, 0, GCM_IV_LENGTH)
        System.arraycopy(payload, GCM_IV_LENGTH, encrypted, 0, encrypted.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun ensureKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
}
