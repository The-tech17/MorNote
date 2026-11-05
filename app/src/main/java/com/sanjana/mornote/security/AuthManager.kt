package com.sanjana.mornote.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthManager(private val prefs: SharedPreferences) {

    companion object {
        private const val PREFS_FILE = "mornote_secure_auth_prefs"
        private const val KEY_PIN_HASH = "master_pin_hash"
        private const val KEY_PIN_SALT = "master_pin_salt"
        private const val KEY_PIN_ITERATIONS = "master_pin_iterations"
        private const val KEY_MNEMONIC_HASH = "mnemonic_hash"
        private const val KEY_MNEMONIC_SALT = "mnemonic_salt"
        private const val KEY_IS_PIN_SET = "is_master_pin_set"

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val DEFAULT_ITERATIONS = 20_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16

        /**
         * Creates an AuthManager backed by EncryptedSharedPreferences on Android.
         * Falls back to standard SharedPreferences if Keystore is unavailable (e.g. in tests).
         */
        fun create(context: Context): AuthManager {
            val sharedPrefs: SharedPreferences = try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (_: Throwable) {
                context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            }
            return AuthManager(sharedPrefs)
        }
    }

    constructor(context: Context) : this(create(context).prefs)

    /**
     * Checks whether a Master PIN has been configured.
     */
    fun isMasterPinSet(): Boolean {
        return prefs.getBoolean(KEY_IS_PIN_SET, false) &&
                prefs.getString(KEY_PIN_HASH, null) != null &&
                prefs.getString(KEY_PIN_SALT, null) != null
    }

    /**
     * Generates a 12-word BIP-39 recovery mnemonic phrase.
     */
    fun generateRecoveryMnemonic(): List<String> {
        return Bip39WordList.generateMnemonic()
    }

    /**
     * Sets up a new 6-digit Master PIN and generates a 12-word BIP-39 recovery mnemonic.
     * Returns the 12-word recovery mnemonic for the user to securely note down.
     */
    fun setupMasterPin(pin: String): List<String> {
        require(pin.length == 6 && pin.all { it.isDigit() }) { "PIN must be exactly 6 digits" }
        val mnemonic = generateRecoveryMnemonic()
        savePinAndMnemonic(pin, mnemonic)
        return mnemonic
    }

    /**
     * Sets up or updates the Master PIN with an explicitly provided 12-word mnemonic.
     */
    fun setupMasterPinWithMnemonic(pin: String, mnemonic: List<String>): Boolean {
        require(pin.length == 6 && pin.all { it.isDigit() }) { "PIN must be exactly 6 digits" }
        if (!Bip39WordList.validateMnemonic(mnemonic)) {
            return false
        }
        savePinAndMnemonic(pin, mnemonic)
        return true
    }

    /**
     * Verifies the provided 6-digit PIN against the stored salted PBKDF2 hash.
     */
    fun verifyMasterPin(pin: String): Boolean {
        if (!isMasterPinSet()) return false
        if (pin.length != 6 || !pin.all { it.isDigit() }) return false

        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val iterations = prefs.getInt(KEY_PIN_ITERATIONS, DEFAULT_ITERATIONS)

        val storedHash = decodeBase64(storedHashBase64)
        val salt = decodeBase64(storedSaltBase64)

        val computedHash = derivePbkdf2(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return MessageDigest.isEqual(storedHash, computedHash)
    }

    /**
     * Verifies whether the provided 12-word mnemonic matches the stored recovery phrase.
     */
    fun recoverWithMnemonic(mnemonic: List<String>): Boolean {
        val storedHashBase64 = prefs.getString(KEY_MNEMONIC_HASH, null) ?: return false
        val storedSaltBase64 = prefs.getString(KEY_MNEMONIC_SALT, null) ?: return false
        val iterations = prefs.getInt(KEY_PIN_ITERATIONS, DEFAULT_ITERATIONS)

        val normalizedMnemonic = mnemonic.joinToString(" ") { it.trim().lowercase() }
        val storedHash = decodeBase64(storedHashBase64)
        val salt = decodeBase64(storedSaltBase64)

        val computedHash = derivePbkdf2(normalizedMnemonic.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return MessageDigest.isEqual(storedHash, computedHash)
    }

    /**
     * Resets the Master PIN to a new 6-digit PIN using the 12-word recovery mnemonic.
     */
    fun resetMasterPinWithMnemonic(mnemonic: List<String>, newPin: String): Boolean {
        require(newPin.length == 6 && newPin.all { it.isDigit() }) { "New PIN must be exactly 6 digits" }
        if (!recoverWithMnemonic(mnemonic)) {
            return false
        }
        savePinAndMnemonic(newPin, mnemonic)
        return true
    }

    /**
     * Clears stored Master PIN and Recovery Mnemonic.
     */
    fun clearMasterPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_ITERATIONS)
            .remove(KEY_MNEMONIC_HASH)
            .remove(KEY_MNEMONIC_SALT)
            .putBoolean(KEY_IS_PIN_SET, false)
            .apply()
    }

    private fun savePinAndMnemonic(pin: String, mnemonic: List<String>) {
        val pinSalt = ByteArray(SALT_LENGTH_BYTES).apply { SecureRandom().nextBytes(this) }
        val pinHash = derivePbkdf2(pin.toCharArray(), pinSalt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS)

        val mnemonicSalt = ByteArray(SALT_LENGTH_BYTES).apply { SecureRandom().nextBytes(this) }
        val normalizedMnemonic = mnemonic.joinToString(" ") { it.trim().lowercase() }
        val mnemonicHash = derivePbkdf2(normalizedMnemonic.toCharArray(), mnemonicSalt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS)

        prefs.edit()
            .putString(KEY_PIN_HASH, encodeBase64(pinHash))
            .putString(KEY_PIN_SALT, encodeBase64(pinSalt))
            .putInt(KEY_PIN_ITERATIONS, DEFAULT_ITERATIONS)
            .putString(KEY_MNEMONIC_HASH, encodeBase64(mnemonicHash))
            .putString(KEY_MNEMONIC_SALT, encodeBase64(mnemonicSalt))
            .putBoolean(KEY_IS_PIN_SET, true)
            .apply()
    }

    private fun derivePbkdf2(chars: CharArray, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
        val spec = PBEKeySpec(chars, salt, iterations, keyLengthBits)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun decodeBase64(str: String): ByteArray {
        return try {
            Base64.decode(str, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getDecoder().decode(str)
        }
    }
}
