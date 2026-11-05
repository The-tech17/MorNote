package com.sanjana.mornote.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CipherEngine {
    private const val TAG = "MornoteCrypto"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MorNoteMasterKey"
    private const val LEGACY_KEY_ALIAS = "mornote_master_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    const val IV_SIZE = 12 // 96-bit recommended IV for GCM
    const val TAG_SIZE_BITS = 128 // 128-bit authentication tag
    const val DECRYPTION_ERROR = "[Decryption Error]"

    private fun logI(tag: String, msg: String) {
        try {
            Log.i(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            Log.e(tag, msg, tr)
        } catch (_: Throwable) {
            println("[$tag] $msg ${tr?.message ?: ""}")
        }
    }

    /**
     * Permanently purges and destroys the hardware master key from AndroidKeyStore.
     */
    fun purgeMasterKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
            if (keyStore.containsAlias(LEGACY_KEY_ALIAS)) {
                keyStore.deleteEntry(LEGACY_KEY_ALIAS)
            }
            logI(TAG, "Hardware KeyStore Master Key successfully purged from AndroidKeyStore.")
        } catch (e: Throwable) {
            logE(TAG, "Failed to purge master key from AndroidKeyStore", e)
        }
    }

    /**
     * Retrieves the master AES key from Android KeyStore, or creates a new
     * hardware-backed 256-bit AES key if one does not already exist.
     */
    fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: (keyStore.getKey(KEY_ALIAS, null) as SecretKey)
    }

    /**
     * Encrypts plaintext using AES-256-GCM backed by Android KeyStore.
     * Generates a 12-byte random IV, produces ciphertext with 128-bit auth tag,
     * and returns the result packed as Base64(IV + CiphertextAndTag).
     */
    fun morph(text: String): String {
        if (text.isEmpty()) return ""

        return try {
            val secretKey = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv ?: throw GeneralSecurityException("Cipher IV generation failed")
            val ciphertextAndTag = cipher.doFinal(text.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + ciphertextAndTag.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertextAndTag, 0, combined, iv.size, ciphertextAndTag.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            logE(TAG, "Encryption failed for payload: $text", e)
            throw RuntimeException("Encryption failed", e)
        }
    }

    /**
     * Decrypts Base64(IV + CiphertextAndTag) payload back to original plaintext string.
     * Handles decryption corruption or tampering gracefully by returning [DECRYPTION_ERROR].
     */
    fun unmorph(morphedText: String): String {
        if (morphedText.isBlank()) return ""

        return try {
            val combined = Base64.decode(morphedText, Base64.DEFAULT)
            val minLength = IV_SIZE + (TAG_SIZE_BITS / 8) // 12 + 16 = 28 bytes
            if (combined.size < minLength) {
                logE(TAG, "Decryption failed: payload size ${combined.size} < minimum $minLength for payload: $morphedText")
                return DECRYPTION_ERROR
            }

            val iv = combined.copyOfRange(0, IV_SIZE)
            val ciphertextAndTag = combined.copyOfRange(IV_SIZE, combined.size)

            val secretKey = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(ciphertextAndTag)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            logE(TAG, "Decryption failed for payload: $morphedText", e)
            DECRYPTION_ERROR
        }
    }
}
