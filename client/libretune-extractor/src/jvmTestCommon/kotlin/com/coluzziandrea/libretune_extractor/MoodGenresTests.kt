package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.util.TestUtil
import com.coluzziandrea.libretune_extractor.util.provideMockClient
import kotlinx.coroutines.test.runTest
import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(Enclosed::class)
class MoodGenresTests {

    @Test
    fun `scrapeMoodGenres should correctly parse genres & moods`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("moodGenres.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val result = scraper.genresMoods()


        assertNotNull(result)
        assertEquals(11, result.moods.size)
        assertEquals(25, result.genres.size)

        assertEquals("Chill", result.moods.firstOrNull()?.name)

        assertEquals("African", result.genres.firstOrNull()?.name)

    }

    @Test
    fun `scrapeMoodGenreCategory should correctly parse country genre`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("countryGenre.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val result = scraper.genreMoodCategory("any_ID")


        assertNotNull(result)

        assertEquals(49, result.songs.size)
        assertEquals("A Bar Song (Tipsy)", result.songs.firstOrNull()?.title)
        assertEquals("Shaboozey", result.songs.firstOrNull()?.artists?.firstOrNull()?.name)
        assertEquals(
            "Where I've Been, Isn't Where I'm Going",
            result.songs.firstOrNull()?.album?.name
        )

        assertEquals(3, result.carousels.size)

        assertEquals("Featured playlists", result.carousels.firstOrNull()?.title)
        assertEquals(48, result.carousels.firstOrNull()?.playlists?.size)
        assertEquals(
            "Country Gold",
            result.carousels.firstOrNull()?.playlists?.firstOrNull()?.name
        )
    }

}