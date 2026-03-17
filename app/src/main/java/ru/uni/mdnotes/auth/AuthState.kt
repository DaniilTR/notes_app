package ru.uni.mdnotes.auth

data class AuthState(
    val isPinSet: Boolean = false,
    val isUnlocked: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
    val errorMessage: String? = null,
)
