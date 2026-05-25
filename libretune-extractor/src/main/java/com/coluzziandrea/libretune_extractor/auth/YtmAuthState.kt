package com.coluzziandrea.libretune_extractor.auth

/**
 * Snapshot of the credentials needed to make authenticated YouTube Music
 * innertube requests on behalf of a signed-in user.
 *
 * Cookies must include either `SAPISID` or `__Secure-3PAPISID` — those are the
 * values hashed into the `Authorization: SAPISIDHASH …` header that YT Music
 * verifies on write endpoints.
 */
data class YtmAuthState(
    val cookieHeader: String,
    val sapisid: String,
    val origin: String = "https://music.youtube.com",
    val userAgent: String = DEFAULT_USER_AGENT,
    val visitorData: String? = null
) {
    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

        fun extractSapisid(cookieHeader: String): String? {
            val parts = cookieHeader.split(';').map { it.trim() }
            val sapisid = parts.firstOrNull { it.startsWith("SAPISID=") }
                ?.removePrefix("SAPISID=")
            if (!sapisid.isNullOrBlank()) return sapisid
            return parts.firstOrNull { it.startsWith("__Secure-3PAPISID=") }
                ?.removePrefix("__Secure-3PAPISID=")
        }
    }
}
