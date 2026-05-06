package com.colux.libretune.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.colux.libretune.desktop.viewmodel.AuthMode
import com.colux.libretune.desktop.viewmodel.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isAuthenticated) {
            AuthenticatedView(
                username = state.username,
                isLoading = state.isLoading,
                onLogout = { viewModel.logout() },
            )
        } else {
            LoginRegisterForm(
                isLoading = state.isLoading,
                onSubmit = { mode, username, password, email ->
                    viewModel.submit(mode, username, password, email)
                },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

@Composable
private fun AuthenticatedView(
    username: String,
    isLoading: Boolean,
    onLogout: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Signed in as $username", style = MaterialTheme.typography.titleMedium)
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                OutlinedButton(onClick = onLogout) { Text("Sign out") }
            }
        }
    }
}

@Composable
private fun LoginRegisterForm(
    isLoading: Boolean,
    onSubmit: (AuthMode, String, String, String?) -> Unit,
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.width(360.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (mode == AuthMode.LOGIN) "Sign in to LibreTune" else "Create an account",
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (mode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        onSubmit(mode, username, password, email.ifBlank { null })
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (mode == AuthMode.LOGIN) "Sign in" else "Register")
                }
            }

            Row(horizontalArrangement = Arrangement.Center) {
                val switchText = if (mode == AuthMode.LOGIN) "New here?" else "Already have an account?"
                val actionText = if (mode == AuthMode.LOGIN) "Register" else "Sign in"
                Text(switchText, style = MaterialTheme.typography.bodySmall)
                TextButton(
                    onClick = {
                        mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                    },
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
