package com.coluzziandrea.libretune_extractor.auth

/**
 * Supplies the current YouTube Music auth state to the network client.
 *
 * Implementations live in the app module (cookie persistence + WebView capture).
 * Returning `null` means the user is signed out — the client falls back to
 * anonymous requests, which is fine for read-only browse/search.
 */
fun interface AuthProvider {
    fun current(): YtmAuthState?

    object Anonymous : AuthProvider {
        override fun current(): YtmAuthState? = null
    }
}
