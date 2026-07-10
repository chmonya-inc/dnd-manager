package com.dnd.helper.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dnd.helper.di.isDesktop
import com.dnd.helper.shared.core.generated.resources.Res
import com.dnd.helper.shared.core.generated.resources.logo
import com.dnd.helper.theme.DndIcons
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    forceMasterRole: Boolean = false,
    onSettingsClick: (() -> Unit)? = null,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(forceMasterRole) {
        if (forceMasterRole) {
            viewModel.onEvent(AuthEvent.SetMasterRole)
        } else {
            viewModel.onEvent(AuthEvent.SetRequiredRole("PLAYER"))
        }
    }

    var showRecoverCodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.registeredRecoverCode) {
        if (state.registeredRecoverCode != null) {
            showRecoverCodeDialog = true
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.registeredRecoverCode == null) {
            onAuthSuccess()
        }
    }

    if (showRecoverCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showRecoverCodeDialog = false
                onAuthSuccess()
            },
            title = { Text("Save your Recovery Code") },
            text = {
                androidx.compose.foundation.text.selection.SelectionContainer {
                    Text(
                        "Your password recovery code is:\n\n${state.registeredRecoverCode}\n\nPlease save this code somewhere safe. You can use it to reset your password later."
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showRecoverCodeDialog = false
                    onAuthSuccess()
                }) {
                    Text("I've saved it")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isDesktop && onSettingsClick != null) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = DndIcons.Filled.Build,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(if (isDesktop) 0.4f else 0.9f)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (state.isRecoverMode) "Recover Password" else if (state.isLoginMode) "Login" else "Register",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { viewModel.onEvent(AuthEvent.OnUsernameChanged(it)) },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(AuthEvent.PasteUsername) }) {
                                Icon(
                                    imageVector = DndIcons.Filled.ContentPaste,
                                    contentDescription = "Paste"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(AuthEvent.OnPasswordChanged(it)) },
                        label = { Text(if (state.isRecoverMode) "Old Password or Recovery Code" else "Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(AuthEvent.PastePassword) }) {
                                Icon(
                                    imageVector = DndIcons.Filled.ContentPaste,
                                    contentDescription = "Paste"
                                )
                            }
                        }
                    )

                    if (state.isRecoverMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = { viewModel.onEvent(AuthEvent.OnNewPasswordChanged(it)) },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onEvent(AuthEvent.PasteNewPassword) }) {
                                    Icon(
                                        imageVector = DndIcons.Filled.ContentPaste,
                                        contentDescription = "Paste"
                                    )
                                }
                            }
                        )
                    }

                    if (!state.isLoginMode && !state.isRecoverMode && forceMasterRole) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Master Account — creates campaigns, characters, monsters, and assigns characters to players.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else if (!state.isLoginMode && !state.isRecoverMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Registering as a Player — you will be able to view and play your assigned characters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    state.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    state.errorRoleMismatch?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.onEvent(AuthEvent.Submit) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.isLoading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (state.isRecoverMode) "Recover" else if (state.isLoginMode) "Login" else "Register",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { viewModel.onEvent(AuthEvent.ToggleMode) },
                        enabled = !state.isLoading
                    ) {
                        Text(
                            if (state.isLoginMode) {
                                "Don't have an account? Register"
                            } else {
                                "Already have an account? Login"
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (state.isLoginMode || state.isRecoverMode) {
                        TextButton(
                            onClick = { viewModel.onEvent(AuthEvent.ToggleRecoverMode) },
                            enabled = !state.isLoading
                        ) {
                            Text(
                                if (state.isRecoverMode) "Back to Login" else "Forgot password? Recover",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
