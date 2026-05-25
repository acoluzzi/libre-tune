package com.colux.libretune.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.coluzziandrea.libretune_extractor.auth.YtmAuthState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the YouTube Music session cookies on disk.
 *
 * Stored in plain `SharedPreferences` for now — the cookies are scoped to this
 * app's data dir and don't leave the device, but moving to
 * `EncryptedSharedPreferences` is a sensible hardening step before shipping.
 */
@Singleton
class YtMusicCredentialsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(state: YtmAuthState) {
        prefs.edit()
            .putString(KEY_COOKIE, state.cookieHeader)
            .putString(KEY_SAPISID, state.sapisid)
            .putString(KEY_ORIGIN, state.origin)
            .putString(KEY_VISITOR_DATA, state.visitorData)
            .apply()
    }

    fun load(): YtmAuthState? {
        val cookie = prefs.getString(KEY_COOKIE, null) ?: return null
        val sapisid = prefs.getString(KEY_SAPISID, null)
            ?: YtmAuthState.extractSapisid(cookie)
            ?: return null
        return YtmAuthState(
            cookieHeader = cookie,
            sapisid = sapisid,
            origin = prefs.getString(KEY_ORIGIN, "https://music.youtube.com")!!,
            visitorData = prefs.getString(KEY_VISITOR_DATA, null)
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "ytm_auth"
        private const val KEY_COOKIE = "cookie"
        private const val KEY_SAPISID = "sapisid"
        private const val KEY_ORIGIN = "origin"
        private const val KEY_VISITOR_DATA = "visitor_data"
    }
}
