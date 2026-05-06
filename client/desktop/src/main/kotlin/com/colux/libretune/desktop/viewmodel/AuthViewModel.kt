package com.colux.libretune.desktop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.repository.BackendSyncRepository
import com.colux.libretune.data.sync.LibrarySyncOrchestrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

enum class AuthMode { LOGIN, REGISTER }

class AuthViewModel(
    private val backend: BackendSyncRepository,
    private val syncOrchestrator: LibrarySyncOrchestrator,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isAuthenticated = backend.isAuthenticated()))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun submit(mode: AuthMode, username: String, password: String, email: String?) {
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Username and password are required.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null, message = null) }
        viewModelScope.launch {
            runCatching {
                when (mode) {
                    AuthMode.LOGIN -> backend.login(username, password)
                    AuthMode.REGISTER -> backend.register(username, password, email)
                }
            }.onSuccess { response ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = response.user.username,
                        error = null,
                        message = "Signed in as ${response.user.username}.",
                    )
                }
                // Trigger a sync immediately after sign-in (replaces WorkManager on desktop)
                viewModelScope.launch { syncOrchestrator.syncAll() }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Authentication failed.",
                    )
                }
            }
        }
    }

    fun logout() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            runCatching { backend.logout() }
            _state.update { AuthUiState(isAuthenticated = false, message = "Signed out.") }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(error = null, message = null) }
    }
}
