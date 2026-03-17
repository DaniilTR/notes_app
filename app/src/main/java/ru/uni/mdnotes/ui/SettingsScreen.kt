package ru.uni.mdnotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import ru.uni.mdnotes.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authStateFlow: StateFlow<AuthState>,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val authState by authStateFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                actions = {
                    TextButton(onClick = onBack) { Text("Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Вход")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Вход по биометрии")

                Switch(
                    checked = authState.biometricEnabled,
                    onCheckedChange = { onSetBiometricEnabled(it) },
                    enabled = authState.biometricAvailable,
                )

                if (!authState.biometricAvailable) {
                    Text("Биометрия недоступна (или не настроена) на устройстве.")
                } else {
                    Text("Если включено, на экране входа будет доступна биометрия.")
                }
            }
        }
    }
}
