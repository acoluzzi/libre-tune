package com.colux.libretune.di

import com.coluzziandrea.libretune_extractor.auth.AuthProvider
import com.colux.libretune.data.remote.auth.YtMusicAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthProvider(impl: YtMusicAuthRepository): AuthProvider
}
