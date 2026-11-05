package com.sanjana.mornote.util

import android.content.Context
import android.net.Uri
import com.sanjana.mornote.NoteRepository
import com.sanjana.mornote.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {

    // Base64 Compatibility layer to avoid Android framework stub exceptions during local JUnit tests
    object Base64Compat {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private val DECODE_TABLE = IntArray(256) { -1 }.apply {
            for (i in ALPHABET.indices) {
                this[ALPHABET[i].code] = i
            }
        }

        fun encode(data: ByteArray): String {
            val sb = StringBuilder((data.size * 4 + 2) / 3)
            var i = 0
            while (i < data.size) {
                val b0 = data[i++].toInt() and 0xFF
                if (i < data.size) {
                    val b1 = data[i++].toInt() and 0xFF
                    if (i < data.size) {
                        val b2 = data[i++].toInt() and 0xFF
                        sb.append(ALPHABET[b0 shr 2])
                        sb.append(ALPHABET[((b0 and 3) shl 4) or (b1 shr 4)])
                        sb.append(ALPHABET[((b1 and 0xF) shl 2) or (b2 shr 6)])
                        sb.append(ALPHABET[b2 and 0x3F])
                    } else {
                        sb.append(ALPHABET[b0 shr 2])
                        sb.append(ALPHABET[((b0 and 3) shl 4) or (b1 shr 4)])
                        sb.append(ALPHABET[(b1 and 0xF) shl 2])
                        sb.append('=')
                    }
                } else {
                    sb.append(ALPHABET[b0 shr 2])
                    sb.append(ALPHABET[(b0 and 3) shl 4])
                    sb.append("==")
                }
            }
            return sb.toString()
        }

        fun decode(src: String): ByteArray {
            val input = src.filter { !it.isWhitespace() }
            var pad = 0
            if (input.endsWith("==")) pad = 2
            else if (input.endsWith("=")) pad = 1
            val len = (input.length * 3) / 4 - pad
            val result = ByteArray(len)
            var i = 0
            var j = 0
            while (i < input.length) {
                val c0 = DECODE_TABLE[input[i++].code]
                val c1 = DECODE_TABLE[input[i++].code]
                val c2 = if (i < input.length && input[i] != '=') DECODE_TABLE[input[i++].code] else { i++; 0 }
                val c3 = if (i < input.length && input[i] != '=') DECODE_TABLE[input[i++].code] else { i++; 0 }
                
                if (c0 < 0 || c1 < 0 || c2 < 0 || c3 < 0) continue
                
                result[j++] = ((c0 shl 2) or (c1 shr 4)).toByte()
                if (j < len) {
                    result[j++] = (((c1 and 0xF) shl 4) or (c2 shr 2)).toByte()
                    if (j < len) {
                        result[j++] = (((c2 and 3) shl 6) or c3).toByte()
                    }
                }
            }
            return result
        }
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plaintext: String, passphrase: String): JSONObject {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val secretKey = deriveKey(passphrase, salt)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        
        val encryptedData = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        val tagLength = 16
        val ciphertextLength = encryptedData.size - tagLength
        val ciphertext = encryptedData.copyOfRange(0, ciphertextLength)
        val authTag = encryptedData.copyOfRange(ciphertextLength, encryptedData.size)
        
        return JSONObject().apply {
            put("salt", Base64Compat.encode(salt))
            put("iv", Base64Compat.encode(iv))
            put("ciphertext", Base64Compat.encode(ciphertext))
            put("authTag", Base64Compat.encode(authTag))
        }
    }

    fun decrypt(jsonObj: JSONObject, passphrase: String): String {
        val salt = Base64Compat.decode(jsonObj.getString("salt"))
        val iv = Base64Compat.decode(jsonObj.getString("iv"))
        val ciphertext = Base64Compat.decode(jsonObj.getString("ciphertext"))
        val authTag = Base64Compat.decode(jsonObj.getString("authTag"))
        
        val encryptedData = ByteArray(ciphertext.size + authTag.size)
        System.arraycopy(ciphertext, 0, encryptedData, 0, ciphertext.size)
        System.arraycopy(authTag, 0, encryptedData, ciphertext.size, authTag.size)
        
        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        
        val decryptedBytes = cipher.doFinal(encryptedData)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    suspend fun exportBackup(context: Context, uri: Uri, passphrase: String, repository: NoteRepository) = withContext(Dispatchers.IO) {
        val notes = repository.getAllNotesRaw()
        val notesArray = JSONArray()
        for (note in notes) {
            val noteJson = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("contentMorse", note.contentMorse)
                put("timestamp", note.timestamp)
                put("isPinned", note.isPinned)
                put("isArchived", note.isArchived)
                val tagsArray = JSONArray()
                note.tags.forEach { tagsArray.put(it) }
                put("tags", tagsArray)
            }
            notesArray.put(noteJson)
        }
        
        val plaintext = notesArray.toString()
        val encryptedJson = encrypt(plaintext, passphrase)
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(encryptedJson.toString().toByteArray(Charsets.UTF_8))
        } ?: throw Exception("Failed to open output stream")
    }

    suspend fun importBackup(context: Context, uri: Uri, passphrase: String, repository: NoteRepository) = withContext(Dispatchers.IO) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: throw Exception("Failed to open input stream")
        
        val encryptedJson = JSONObject(jsonString)
        val decryptedPlaintext = decrypt(encryptedJson, passphrase)
        
        val notesArray = JSONArray(decryptedPlaintext)
        val notes = mutableListOf<NoteEntity>()
        for (i in 0 until notesArray.length()) {
            val noteJson = notesArray.getJSONObject(i)
            val tagsArray = noteJson.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (j in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(j))
                }
            }
            val note = NoteEntity(
                id = noteJson.getInt("id"),
                title = noteJson.getString("title"),
                contentMorse = noteJson.getString("contentMorse"),
                timestamp = noteJson.getLong("timestamp"),
                isPinned = noteJson.optBoolean("isPinned", false),
                isArchived = noteJson.optBoolean("isArchived", false),
                tags = tags
            )
            notes.add(note)
        }
        
        repository.insertAll(notes)
    }
}
