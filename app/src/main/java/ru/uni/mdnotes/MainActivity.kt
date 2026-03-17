package ru.uni.mdnotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.uni.mdnotes.auth.AuthViewModel
import ru.uni.mdnotes.notes.FileNotesRepository
import ru.uni.mdnotes.notes.NotesViewModel
import ru.uni.mdnotes.ui.EditorScreen
import ru.uni.mdnotes.ui.LockScreen
import ru.uni.mdnotes.ui.NotesListScreen
import ru.uni.mdnotes.ui.SettingsScreen
import ru.uni.mdnotes.ui.theme.MdNotesTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MdNotesTheme {
                val context = LocalContext.current

                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.state.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.refreshBiometricAvailability(this@MainActivity)
                }

                val notesRepository = remember { FileNotesRepository(context) }
                val notesViewModel: NotesViewModel = viewModel(
                    factory = NotesViewModel.factory(notesRepository)
                )

                if (!authState.isUnlocked) {
                    LockScreen(
                        authState = authState,
                        onSetupPin = { pin -> authViewModel.setNewPin(pin) },
                        onUnlockWithPin = { pin ->
                            authViewModel.tryUnlockWithPin(pin)
                        },
                        onUnlockWithBiometric = { activity ->
                            authViewModel.tryUnlockWithBiometric(activity)
                        },
                    )
                } else {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "notes",
                    ) {
                        composable("notes") {
                            NotesListScreen(
                                notesStateFlow = notesViewModel.state,
                                onCreateNew = {
                                    val noteId = notesViewModel.createNewNote()
                                    navController.navigate("edit/$noteId")
                                },
                                onOpen = { noteId ->
                                    navController.navigate("edit/$noteId")
                                },
                                onDelete = { noteId ->
                                    notesViewModel.deleteNote(noteId)
                                },
                                onOpenSettings = { navController.navigate("settings") },
                                onExit = { authViewModel.lock() },
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                authStateFlow = authViewModel.state,
                                onSetBiometricEnabled = { enabled ->
                                    authViewModel.setBiometricEnabled(enabled)
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(
                            route = "edit/{noteId}",
                            arguments = listOf(
                                navArgument("noteId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")
                                ?: return@composable

                            EditorScreen(
                                noteId = noteId,
                                notesStateFlow = notesViewModel.state,
                                onLoad = { notesViewModel.loadNote(noteId) },
                                onSave = { content -> notesViewModel.saveNote(noteId, content) },
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
