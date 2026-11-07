package com.taskora.app

import android.content.Context
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Three rotating, app-private AES-256-GCM backup slots protected by a user password. */
object EncryptedBackupStore {
    private val magic = "TASKORAE1".toByteArray(Charsets.US_ASCII)
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val KEY_BITS = 256
    private const val ITERATIONS = 150_000

    fun file(context: Context, slot: Int): File {
        require(slot in 1..3)
        return File(context.filesDir, "taskora_backup_slot_$slot.tkb")
    }

    fun save(context: Context, slot: Int, password: CharArray, plain: ByteArray) {
        require(password.size >= 8) { "Password must contain at least 8 characters." }
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val iv = ByteArray(IV_SIZE).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, derive(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(magic)
        val encrypted = cipher.doFinal(plain)
        val target = file(context, slot)
        val temporary = File(context.filesDir, target.name + ".tmp")
        temporary.outputStream().use { out: java.io.OutputStream ->
            out.write(magic); out.write(salt); out.write(iv); out.write(encrypted)
        }
        try {
            android.system.Os.rename(temporary.absolutePath, target.absolutePath)
        } catch (error: Exception) {
            temporary.delete()
            throw IllegalStateException("Could not atomically save backup slot.", error)
        }
    }

    fun restore(context: Context, slot: Int, password: CharArray): ByteArray {
        val bytes = file(context, slot).readBytes()
        require(bytes.size > magic.size + SALT_SIZE + IV_SIZE + 16) { "Backup slot is empty or damaged." }
        require(bytes.copyOfRange(0, magic.size).contentEquals(magic)) { "This is not an encrypted Taskora backup." }
        val saltStart = magic.size
        val ivStart = saltStart + SALT_SIZE
        val cipherStart = ivStart + IV_SIZE
        val salt = bytes.copyOfRange(saltStart, ivStart)
        val iv = bytes.copyOfRange(ivStart, cipherStart)
        val encrypted = bytes.copyOfRange(cipherStart, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, derive(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(magic)
        return cipher.doFinal(encrypted)
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
