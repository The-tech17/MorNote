package com.sanjana.mornote

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sanjana.mornote.util.CipherEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

@RunWith(AndroidJUnit4::class)
class CipherEngineInstrumentedTest {

    @Test
    fun testKeyStoreInitialization() {
        val secretKey = CipherEngine.getOrCreateKey()
        assertNotNull(secretKey)

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val containsKey = keyStore.containsAlias("MorNoteMasterKey") || keyStore.containsAlias("mornote_master_key")
        assertTrue("KeyStore must contain master key alias", containsKey)
    }

    @Test
    fun testEncryptionDecryptionRoundTripUtf8() {
        val testCases = listOf(
            "Hello World 123!",
            "Top Secret 🔒 12345 🔑 - confidential personal note.",
            "Special characters: ÇÖü ß åñá 漢字 العربية \uD83D\uDE80",
            "Multi-line\nNote\nWith\r\nTabs\tAnd Spaces    .",
            "{\"key\":\"value\",\"notes\":[1,2,3]}"
        )

        for (plainText in testCases) {
            val encryptedBase64 = CipherEngine.morph(plainText)
            assertNotEquals("Encrypted text must not match plaintext", plainText, encryptedBase64)
            assertTrue("Encrypted payload must not be empty", encryptedBase64.isNotEmpty())

            val decryptedText = CipherEngine.unmorph(encryptedBase64)
            assertEquals("Decrypted text must match original plaintext", plainText, decryptedText)
        }
    }

    @Test
    fun testRandomIvPerEncryption() {
        val plainText = "Deterministic Input Note"
        val encrypted1 = CipherEngine.morph(plainText)
        val encrypted2 = CipherEngine.morph(plainText)

        // AES-GCM with fresh 12-byte IV must produce distinct ciphertexts
        assertNotEquals("Subsequent encryptions of same text must have unique ciphertexts", encrypted1, encrypted2)
        assertEquals(plainText, CipherEngine.unmorph(encrypted1))
        assertEquals(plainText, CipherEngine.unmorph(encrypted2))
    }

    @Test
    fun testTamperedCiphertextReturnsErrorState() {
        val originalText = "Confidential financial record 9876-5432-1098"
        val encryptedBase64 = CipherEngine.morph(originalText)

        // Tamper with a single character in the Base64 ciphertext
        val chars = encryptedBase64.toCharArray()
        val tamperIndex = chars.size / 2
        chars[tamperIndex] = if (chars[tamperIndex] == 'A') 'B' else 'A'
        val tamperedBase64 = String(chars)

        // unmorph must gracefully catch AEADBadTagException / error and return [Decryption Error]
        val result = CipherEngine.unmorph(tamperedBase64)
        assertEquals("Decryption of tampered ciphertext must return explicit error state", CipherEngine.DECRYPTION_ERROR, result)
    }

    @Test
    fun testTamperedPayloadThrowsAEADBadTagException() {
        val originalText = "Sensitive user authentication token"
        val encryptedBase64 = CipherEngine.morph(originalText)
        val decoded = Base64.decode(encryptedBase64, Base64.DEFAULT)

        assertTrue(decoded.size >= CipherEngine.IV_SIZE + (CipherEngine.TAG_SIZE_BITS / 8))

        val iv = decoded.copyOfRange(0, CipherEngine.IV_SIZE)
        val ciphertextAndTag = decoded.copyOfRange(CipherEngine.IV_SIZE, decoded.size)

        // Flip a bit in the ciphertext / tag
        ciphertextAndTag[ciphertextAndTag.size - 1] = (ciphertextAndTag[ciphertextAndTag.size - 1].toInt() xor 0x01).toByte()

        val secretKey = CipherEngine.getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(CipherEngine.TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        // Modifying any byte of ciphertext or tag MUST cause GCM authentication tag verification failure
        assertThrows(AEADBadTagException::class.java) {
            cipher.doFinal(ciphertextAndTag)
        }
    }

    @Test
    fun testCorruptedAndInvalidPayloads() {
        assertEquals("", CipherEngine.morph(""))
        assertEquals("", CipherEngine.unmorph(""))
        assertEquals("", CipherEngine.unmorph("   "))

        // Short corrupted Base64 payload (less than IV + Auth tag)
        assertEquals(CipherEngine.DECRYPTION_ERROR, CipherEngine.unmorph("SGVsbG8="))
        // Invalid Base64 string
        assertEquals(CipherEngine.DECRYPTION_ERROR, CipherEngine.unmorph("!!!invalid_base64@@@"))
    }

    @Test
    fun testPurgeMasterKey() {
        val originalText = "Top secret payload to be destroyed"
        val morphedText = CipherEngine.morph(originalText)
        assertNotEquals(originalText, morphedText)

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val containsAliasBefore = keyStore.containsAlias("MorNoteMasterKey") || keyStore.containsAlias("mornote_master_key")
        assertTrue("KeyStore must contain master key before purge", containsAliasBefore)

        // Purge KeyStore master key
        CipherEngine.purgeMasterKey()

        val containsAliasAfterMorNoteMasterKey = keyStore.containsAlias("MorNoteMasterKey")
        val containsAliasAfterLegacyKey = keyStore.containsAlias("mornote_master_key")
        assertFalse("KeyStore alias MorNoteMasterKey must evaluate to false after purgeMasterKey()", containsAliasAfterMorNoteMasterKey)
        assertFalse("KeyStore legacy alias must evaluate to false after purgeMasterKey()", containsAliasAfterLegacyKey)

        // Decryption of previous payload must fail / return error state since key pointer was destroyed
        val unmorphedText = CipherEngine.unmorph(morphedText)
        assertNotEquals("Decryption of previously morphed text must fail after key purge", originalText, unmorphedText)
        assertEquals("Decryption attempt on purged key must yield DECRYPTION_ERROR", CipherEngine.DECRYPTION_ERROR, unmorphedText)
    }
}
