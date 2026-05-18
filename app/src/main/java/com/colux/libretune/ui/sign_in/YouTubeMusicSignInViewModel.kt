package com.colux.libretune.ui.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.remote.auth.YtMusicAuthRepository
import com.colux.libretune.data.repository.YouTubeMusicSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YouTubeMusicSignInViewModel @Inject constructor(
    private val authRepository: YtMusicAuthRepository,
    private val syncRepository: YouTubeMusicSyncRepository,
) : ViewModel() {

    val isSignedIn: StateFlow<Boolean> = authRepository.state
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.current() != null)

    fun captureCookies(cookieHeader: String) {
        viewModelScope.launch {
            runCatching { authRepository.signIn(cookieHeader) }
                .onSuccess {
                    // Drain any offline queue immediately after sign-in.
                    syncRepository.flushPendingMutations()
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
