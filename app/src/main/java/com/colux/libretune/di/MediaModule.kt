package com.colux.libretune.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {


    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        // 1. Define the audio attributes for music playback.
        // This tells the system what kind of audio you are playing.
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            // 2. Set the audio attributes on the player.
            // The 'true' enables automatic audio focus handling.
            .setAudioAttributes(audioAttributes, true)

            // 3. Also configure the player to pause when headphones are unplugged.
            .setHandleAudioBecomingNoisy(true)

            .build()
    }
}