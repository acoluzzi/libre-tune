package com.colux.libretune.data.remote.backend

import java.util.prefs.Preferences

class DesktopBackendTokenStore : BackendTokenStore {

    private val prefs: Preferences =
        Preferences.userRoot().node("com/colux/libretune/auth")

    override fun save(token: String) {
        prefs.put(KEY_AUTH_TOKEN, token)
        prefs.flush()
    }

    override fun get(): String? =
        prefs.get(KEY_AUTH_TOKEN, null)?.ifEmpty { null }

    override fun clear() {
        prefs.remove(KEY_AUTH_TOKEN)
        prefs.flush()
    }

    override fun isAuthenticated(): Boolean = !get().isNullOrEmpty()

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}
