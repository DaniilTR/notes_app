package ru.uni.mdnotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import ru.uni.mdnotes.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    authState: AuthState,
    onSetupPin: (pin: String) -> Unit,
    onUnlockWithPin: (pin: String) -> Unit,
    onUnlockWithBiometric: (activity: FragmentActivity) -> Unit,
) {
    val activity = LocalContext.current as? FragmentActivity

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (authState.isPinSet) "Вход" else "Создание ПИН") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!authState.isPinSet) {
                SetupPinContent(
                    onSetupPin = onSetupPin,
                )
            } else {
                UnlockContent(
                    activity = activity,
                    authState = authState,
                    onUnlockWithPin = onUnlockWithPin,
                    onUnlockWithBiometric = onUnlockWithBiometric,
                )
            }

            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SetupPinContent(
    onSetupPin: (pin: String) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Text("Задайте ПИН-код для входа.")

    OutlinedTextField(
        value = pin,
        onValueChange = { pin = it.filter(Char::isDigit).take(12) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("ПИН") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
    )

    OutlinedTextField(
        value = pin2,
        onValueChange = { pin2 = it.filter(Char::isDigit).take(12) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Повторите ПИН") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
    )

    Text("Вход по биометрии можно включить позже в настройках (если устройство поддерживает).")

    if (localError != null) {
        Text(localError!!)
    }

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            localError = null
            when {
                pin.length < 4 -> localError = "ПИН должен быть минимум 4 цифры"
                pin != pin2 -> localError = "ПИН-коды не совпадают"
                else -> onSetupPin(pin)
            }
        }
    ) {
        Text("Сохранить ПИН")
    }
}

@Composable
private fun UnlockContent(
    activity: FragmentActivity?,
    authState: AuthState,
    onUnlockWithPin: (pin: String) -> Unit,
    onUnlockWithBiometric: (activity: FragmentActivity) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var didAutoPrompt by remember { mutableStateOf(false) }

    // Автопредложение биометрии при открытии (если включена и доступна)
    LaunchedEffect(authState.biometricAvailable, authState.biometricEnabled) {
        if (!didAutoPrompt && activity != null && authState.biometricAvailable && authState.biometricEnabled) {
            didAutoPrompt = true
            onUnlockWithBiometric(activity)
        }
    }

    OutlinedTextField(
        value = pin,
        onValueChange = { pin = it.filter(Char::isDigit).take(12) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("ПИН") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
    )

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onUnlockWithPin(pin) },
    ) {
        Text("Войти по ПИН")
    }

    // Ниже ПИН: подсказка + кнопка биометрии (если включено в настройках)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Войти можно по биометрии, если она включена в настройках.")

    if (authState.biometricAvailable) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { if (activity != null) onUnlockWithBiometric(activity) },
            enabled = authState.biometricEnabled && activity != null,
        ) {
            Text("Войти по биометрии")
        }
    } else {
        Text("Биометрия недоступна на устройстве.")
    }
}
