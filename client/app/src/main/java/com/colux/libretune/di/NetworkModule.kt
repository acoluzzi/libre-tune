package com.colux.libretune.di

import com.colux.libretune.BuildConfig
import com.colux.libretune.data.remote.backend.AndroidBackendTokenStore
import com.colux.libretune.data.remote.backend.BackendApi
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "type"
    }

    @Provides
    @Singleton
    fun provideKtorClient(json: Json): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }

    @Provides
    @Singleton
    fun provideLibreTuneExtractor(httpClient: HttpClient): LibreTuneExtractor =
        LibreTuneExtractor(httpClient)

    @Provides
    @Singleton
    fun provideBackendTokenStore(impl: AndroidBackendTokenStore): BackendTokenStore = impl

    @Provides
    @Singleton
    fun provideBackendApi(
        httpClient: HttpClient,
        tokenStore: BackendTokenStore,
    ): BackendApi = BackendApi(httpClient, tokenStore, BuildConfig.BACKEND_BASE_URL)
}