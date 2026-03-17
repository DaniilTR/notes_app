package ru.uni.mdnotes.auth

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val store = AuthStore(application)

    private val _state = MutableStateFlow(
        AuthState(
            isPinSet = store.isPinSet(),
            isUnlocked = false,
            biometricAvailable = false,
            biometricEnabled = store.isBiometricEnabled(),
            errorMessage = null,
        )
    )

    val state: StateFlow<AuthState> = _state

    fun refreshBiometricAvailability(activity: FragmentActivity) {
        val available = BiometricAuth.isBiometricAvailable(activity)
        _state.update {
            it.copy(
                biometricAvailable = available,
                biometricEnabled = if (available) it.biometricEnabled else false,
            )
        }
        if (!available && store.isBiometricEnabled()) {
            store.setBiometricEnabled(false)
        }
    }

    fun setNewPin(pin: String) {
        store.setNewPin(pin.toCharArray())
        _state.update {
            it.copy(
                isPinSet = true,
                biometricEnabled = store.isBiometricEnabled(),
                errorMessage = null,
            )
        }
    }

    fun tryUnlockWithPin(pin: String) {
        val ok = store.verifyPin(pin.toCharArray())
        _state.update {
            if (ok) {
                it.copy(isUnlocked = true, errorMessage = null)
            } else {
                it.copy(errorMessage = "Неверный ПИН")
            }
        }
    }

    fun tryUnlockWithBiometric(activity: FragmentActivity) {
        refreshBiometricAvailability(activity)

        val current = _state.value
        if (!current.biometricAvailable) {
            _state.update { it.copy(errorMessage = "Биометрия недоступна на устройстве") }
            return
        }
        if (!current.biometricEnabled) {
            _state.update { it.copy(errorMessage = "Биометрия отключена в настройке входа") }
            return
        }

        BiometricAuth.prompt(
            activity = activity,
            title = "Вход",
            subtitle = "Подтвердите вход по биометрии",
            negativeText = "Ввести ПИН",
            onSuccess = {
                _state.update { it.copy(isUnlocked = true, errorMessage = null) }
            },
            onError = { msg ->
                _state.update { it.copy(errorMessage = msg) }
            }
        )
    }

    fun setBiometricEnabled(enabled: Boolean) {
        store.setBiometricEnabled(enabled)
        _state.update { it.copy(biometricEnabled = enabled, errorMessage = null) }
    }

    fun lock() {
        _state.update { it.copy(isUnlocked = false, errorMessage = null) }
    }
}
