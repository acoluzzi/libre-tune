package com.colux.libretune.data.remote.auth

import com.coluzziandrea.libretune_extractor.auth.AuthProvider
import com.coluzziandrea.libretune_extractor.auth.YtmAuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for YT Music auth state in the app.
 *
 * It both persists credentials (delegating to [YtMusicCredentialsStore]) and
 * exposes itself as an [AuthProvider] so the extractor module can request the
 * current state on every outgoing request without a Hilt dependency on app
 * code.
 */
@Singleton
class YtMusicAuthRepository @Inject constructor(
    private val store: YtMusicCredentialsStore
) : AuthProvider {

    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<YtmAuthState?> = _state.asStateFlow()

    fun signIn(cookieHeader: String, visitorData: String? = null) {
        val sapisid = YtmAuthState.extractSapisid(cookieHeader)
            ?: error("Could not find SAPISID cookie — sign-in did not complete")
        val newState = YtmAuthState(
            cookieHeader = cookieHeader,
            sapisid = sapisid,
            visitorData = visitorData
        )
        store.save(newState)
        _state.value = newState
    }

    fun signOut() {
        store.clear()
        _state.value = null
    }

    override fun current(): YtmAuthState? = _state.value
}
