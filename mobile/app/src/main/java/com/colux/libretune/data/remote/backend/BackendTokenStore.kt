package com.colux.libretune.data.remote.backend

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Persists the auth token returned by the LibreTune backend so that the
 * mobile app can call sync endpoints across app restarts.
 */
@Singleton
class BackendTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun get(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isAuthenticated(): Boolean = !get().isNullOrEmpty()

    private companion object {
        const val PREFS_NAME = "libretune_backend_auth"
        const val KEY_TOKEN = "auth_token"
    }
}
