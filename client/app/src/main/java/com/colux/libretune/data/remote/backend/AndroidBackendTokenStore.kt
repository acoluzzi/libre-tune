package com.colux.libretune.data.remote.backend

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Android implementation of [BackendTokenStore] backed by SharedPreferences,
 * so the mobile app can call backend sync endpoints across restarts.
 */
@Singleton
class AndroidBackendTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : BackendTokenStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun save(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun get(): String? = prefs.getString(KEY_TOKEN, null)

    override fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    override fun isAuthenticated(): Boolean = !get().isNullOrEmpty()

    private companion object {
        const val PREFS_NAME = "libretune_backend_auth"
        const val KEY_TOKEN = "auth_token"
    }
}
