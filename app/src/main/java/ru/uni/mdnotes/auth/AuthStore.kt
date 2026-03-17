package ru.uni.mdnotes.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Base64

class AuthStore(context: Context) {
    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            "auth_store",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)

    fun setNewPin(pin: CharArray) {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.getEncoder().encodeToString(salt))
            .putString(KEY_PIN_HASH, Base64.getEncoder().encodeToString(hash))
            .apply()
    }

    fun verifyPin(pin: CharArray): Boolean {
        val saltB64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val hashB64 = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val salt = Base64.getDecoder().decode(saltB64)
        val expectedHash = Base64.getDecoder().decode(hashB64)
        val actualHash = PinHasher.hashPin(pin, salt)
        return PinHasher.constantTimeEquals(expectedHash, actualHash)
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    private companion object {
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
