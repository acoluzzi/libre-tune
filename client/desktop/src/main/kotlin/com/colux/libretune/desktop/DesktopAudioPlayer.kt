package com.colux.libretune.desktop

import java.util.logging.Logger

/**
 * Plays audio on desktop by delegating to an external player process (mpv or ffplay).
 * Pause/resume use SIGSTOP/SIGCONT on POSIX systems (Linux, macOS).
 */
class DesktopAudioPlayer {

    private val logger = Logger.getLogger(DesktopAudioPlayer::class.java.name)

    private val playerCmd: String? = findPlayer()
    private var process: Process? = null

    private fun findPlayer(): String? {
        for (cmd in listOf("mpv", "ffplay")) {
            try {
                val result = ProcessBuilder("which", cmd).start()
                result.waitFor()
                if (result.exitValue() == 0) {
                    logger.info("Using audio player: $cmd")
                    return cmd
                }
            } catch (_: Exception) {}
        }
        logger.warning("No supported audio player found (tried mpv, ffplay). Install one to enable playback.")
        return null
    }

    fun play(url: String) {
        stop()
        val cmd = playerCmd ?: run {
            logger.severe("Cannot play: no audio player available")
            return
        }
        val command = when (cmd) {
            "mpv" -> listOf("mpv", "--no-video", "--really-quiet", url)
            "ffplay" -> listOf("ffplay", "-nodisp", "-autoexit", "-loglevel", "quiet", url)
            else -> return
        }
        logger.info("Spawning: $command")
        process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
    }

    fun pause() {
        val pid = process?.pid() ?: return
        ProcessBuilder("kill", "-STOP", pid.toString()).start()
    }

    fun resume() {
        val pid = process?.pid() ?: return
        ProcessBuilder("kill", "-CONT", pid.toString()).start()
    }

    fun stop() {
        process?.destroyForcibly()
        process = null
    }

    val isAvailable: Boolean get() = playerCmd != null
}
