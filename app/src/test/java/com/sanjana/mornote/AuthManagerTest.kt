package com.sanjana.mornote

import android.content.SharedPreferences
import com.sanjana.mornote.security.AuthManager
import com.sanjana.mornote.security.Bip39WordList
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthManagerTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        authManager = AuthManager(fakePrefs)
    }

    @Test
    fun testInitialState_noPinConfigured() {
        assertFalse(authManager.isMasterPinSet())
        assertFalse(authManager.verifyMasterPin("123456"))
    }

    @Test
    fun testSetupAndVerifyMasterPin_success() {
        val mnemonic = authManager.setupMasterPin("654321")
        assertTrue(authManager.isMasterPinSet())
        assertEquals(12, mnemonic.size)

        // Verifying with correct PIN succeeds
        assertTrue(authManager.verifyMasterPin("654321"))

        // Verifying with wrong PIN fails
        assertFalse(authManager.verifyMasterPin("123456"))
        assertFalse(authManager.verifyMasterPin("000000"))
    }

    @Test
    fun testBip39MnemonicGenerationAndValidation() {
        val mnemonic = authManager.generateRecoveryMnemonic()
        assertEquals(12, mnemonic.size)

        // Validate that all words are valid BIP-39 words and checksum passes
        assertTrue(Bip39WordList.validateMnemonic(mnemonic))

        // Tampering with one word should fail validation
        val tamperedMnemonic = mnemonic.toMutableList().apply {
            this[0] = if (this[0] == "abandon") "ability" else "abandon"
        }
        assertFalse(Bip39WordList.validateMnemonic(tamperedMnemonic))
    }

    @Test
    fun testRecoverWithMnemonic_successAndFailure() {
        val mnemonic = authManager.setupMasterPin("112233")
        assertTrue(authManager.recoverWithMnemonic(mnemonic))

        // Altering any word should fail recovery
        val invalidMnemonic = mnemonic.toMutableList()
        invalidMnemonic[0] = "zebra"
        assertFalse(authManager.recoverWithMnemonic(invalidMnemonic))

        val wrongLengthMnemonic = mnemonic.take(11)
        assertFalse(authManager.recoverWithMnemonic(wrongLengthMnemonic))
    }

    @Test
    fun testResetMasterPinWithMnemonic_updatesPin() {
        val mnemonic = authManager.setupMasterPin("111111")
        assertTrue(authManager.verifyMasterPin("111111"))

        // Reset PIN using valid recovery mnemonic
        val resetSuccess = authManager.resetMasterPinWithMnemonic(mnemonic, "999999")
        assertTrue(resetSuccess)

        // Old PIN should now fail
        assertFalse(authManager.verifyMasterPin("111111"))

        // New PIN should succeed
        assertTrue(authManager.verifyMasterPin("999999"))
    }

    @Test
    fun testResetMasterPinWithInvalidMnemonic_fails() {
        val mnemonic = authManager.setupMasterPin("123456")
        val fakeMnemonic = listOf(
            "abandon", "abandon", "abandon", "abandon",
            "abandon", "abandon", "abandon", "abandon",
            "abandon", "abandon", "abandon", "about"
        )

        val resetSuccess = authManager.resetMasterPinWithMnemonic(fakeMnemonic, "888888")
        assertFalse(resetSuccess)
        assertTrue(authManager.verifyMasterPin("123456"))
        assertFalse(authManager.verifyMasterPin("888888"))
    }

    @Test
    fun testInvalidPinLength_throwsException() {
        try {
            authManager.setupMasterPin("12345") // 5 digits
            fail("Expected IllegalArgumentException for 5-digit PIN")
        } catch (_: IllegalArgumentException) {
        }

        try {
            authManager.setupMasterPin("1234567") // 7 digits
            fail("Expected IllegalArgumentException for 7-digit PIN")
        } catch (_: IllegalArgumentException) {
        }

        try {
            authManager.setupMasterPin("12345a") // Non-digit
            fail("Expected IllegalArgumentException for non-digit PIN")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun testClearMasterPin() {
        authManager.setupMasterPin("123456")
        assertTrue(authManager.isMasterPinSet())

        authManager.clearMasterPin()
        assertFalse(authManager.isMasterPinSet())
        assertFalse(authManager.verifyMasterPin("123456"))
    }

    @Test
    fun testSaltUniqueness() {
        val prefs1 = FakeSharedPreferences()
        val prefs2 = FakeSharedPreferences()
        val auth1 = AuthManager(prefs1)
        val auth2 = AuthManager(prefs2)

        val pin = "123456"
        auth1.setupMasterPin(pin)
        auth2.setupMasterPin(pin)

        val hash1 = prefs1.getString("master_pin_hash", null)
        val hash2 = prefs2.getString("master_pin_hash", null)
        val salt1 = prefs1.getString("master_pin_salt", null)
        val salt2 = prefs2.getString("master_pin_salt", null)

        assertNotNull(hash1)
        assertNotNull(hash2)
        assertNotNull(salt1)
        assertNotNull(salt2)

        // Unique salts must produce different hashes even for the same PIN
        assertNotEquals(salt1, salt2)
        assertNotEquals(hash1, hash2)
    }

    // In-memory SharedPreferences for clean unit testing without Robolectric or Android Keystore
    class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = HashMap(map)
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)

        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class FakeEditor(private val targetMap: MutableMap<String, Any?>) : SharedPreferences.Editor {
            private val tempMap = mutableMapOf<String, Any?>()
            private val removedKeys = mutableSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) tempMap[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) removedKeys.add(key)
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearAll = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearAll) targetMap.clear()
                removedKeys.forEach { targetMap.remove(it) }
                targetMap.putAll(tempMap)
            }
        }
    }
}
