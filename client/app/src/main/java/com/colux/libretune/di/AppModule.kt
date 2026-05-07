package com.colux.libretune.di

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.colux.libretune.BuildConfig
import com.colux.libretune.data.sync.LibrarySyncWorker
import com.colux.libretune.ui.add_to_playlist.AddToPlaylistViewModel
import com.colux.libretune.ui.artist.ArtistViewModel
import com.colux.libretune.ui.auth.AuthViewModel
import com.colux.libretune.ui.create_playlist.CreateNewPlaylistViewModel
import com.colux.libretune.ui.discography.DiscographyViewModel
import com.colux.libretune.ui.history.HistoryViewModel
import com.colux.libretune.ui.home.HomeViewModel
import com.colux.libretune.ui.library.LibraryViewModel
import com.colux.libretune.ui.mood_genre.MoodGenreViewModel
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.playlist.PlaylistDetailViewModel
import com.colux.libretune.ui.search.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Android-app-level bindings: the BuildConfig backend URL, ExoPlayer, the
 * WorkManager worker, and every Hilt-replaced ViewModel.
 *
 * SavedStateHandle is auto-resolved by Koin's ViewModelFactory when a
 * ViewModel asks for one via get(), so no per-VM `parameter` block is needed.
 */
val appModule = module {
    single<String>(named(BACKEND_BASE_URL_QUALIFIER)) { BuildConfig.BACKEND_BASE_URL }

    single<ExoPlayer> {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        ExoPlayer.Builder(androidApplication())
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    worker { LibrarySyncWorker(get(), get(), get()) }

    viewModel { SearchViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { CreateNewPlaylistViewModel(get()) }
    viewModel { AuthViewModel(get(), androidApplication()) }
    viewModel { PlayerViewModel(androidApplication(), get()) }

    viewModel { AddToPlaylistViewModel(get(), get(), get()) }
    viewModel { ArtistViewModel(get(), get(), get()) }
    viewModel { DiscographyViewModel(get(), get()) }
    viewModel { MoodGenreViewModel(get(), get()) }
    viewModel { PlaylistDetailViewModel(get(), get()) }
}
