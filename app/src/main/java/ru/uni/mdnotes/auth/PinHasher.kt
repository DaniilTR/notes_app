package ru.uni.mdnotes.auth

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val Iterations = 120_000
    private const val KeyLengthBits = 256

    fun newSalt(bytes: Int = 16): ByteArray {
        val salt = ByteArray(bytes)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun hashPin(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin, salt, Iterations, KeyLengthBits)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
    }

    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
