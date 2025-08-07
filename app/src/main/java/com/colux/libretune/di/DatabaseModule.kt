package com.colux.libretune.di

import android.content.Context
import androidx.room.Room
import com.colux.libretune.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "libretune_db"
        ).build()
    }


    @Provides
    @Singleton
    fun provideLikedSongDao(database: AppDatabase) = database.likedSongDao()


}