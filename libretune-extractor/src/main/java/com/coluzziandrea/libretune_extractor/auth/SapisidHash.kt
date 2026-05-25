package com.coluzziandrea.libretune_extractor.auth

import java.security.MessageDigest

/**
 * Builds the `Authorization: SAPISIDHASH <timestamp>_<sha1>` value that
 * Google's "G-style" web authentication expects.
 *
 * Reference: https://stackoverflow.com/a/32065323 — the same scheme YT, YT Music
 * and other Google products use for SAPISID-cookie based auth.
 */
object SapisidHash {

    fun authorizationHeader(
        sapisid: String,
        origin: String = "https://music.youtube.com",
        timestampSeconds: Long = System.currentTimeMillis() / 1000
    ): String {
        val payload = "$timestampSeconds $sapisid $origin"
        val digest = MessageDigest.getInstance("SHA-1").digest(payload.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        return "SAPISIDHASH ${timestampSeconds}_$hex"
    }
}
