package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.coluzziandrea.libretune_extractor.util.TestUtil
import com.coluzziandrea.libretune_extractor.util.provideMockClient
import kotlinx.coroutines.test.runTest
import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(Enclosed::class)
class SearchSuggestionsTests {

    @Test
    fun `scrapeSearch should correctly parse Bill & search suggestions`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("searchSuggestionBillAnd.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val result = scraper.searchSuggestions("any_id")


        assertEquals(9, result.size)

        assertEquals("bill & mckenna medley - the time of my life", result[0].suggestion)
        assertEquals(
            "(i've had) the time of my life bill medley & jennifer warnes",
            result[1].suggestion
        )

        val songResult = result[6].musicItem
        assertNotNull(songResult)
        assertTrue(
            songResult is GenericMusicItem.SongResult
        )
        assertEquals(
            "(I've Had) The Time Of My Life (From \"Dirty Dancing\" Soundtrack)",
            songResult.song?.title
        )
        assertEquals(
            "6eyCDj1s4NI",
            songResult.song?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/O2v_dQNw3znZmI0xYQwhbVVVp0rR3lCAKthgH95ggmXSZc9nzz7EZK4uLDvMvnMZnKtSIeor2sH-Ci3P=w60-h60-l90-rj",
            songResult.song?.images?.firstOrNull()?.url
        )


        val artist = result[8].musicItem
        assertNotNull(artist)
        assertTrue(
            artist is GenericMusicItem.ArtistResult
        )
        assertEquals(
            "Bill Haley & His Comets",
            artist.artist?.name
        )
        assertEquals(
            "UCPDGv4ylKUJkLuEi1iJAq5Q",
            artist.artist?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/cdhjabOHZgTkvDTMx7wpn8bFXjwEcxk6nZUDJdd-8-KiqoRQXqgrn_-26AS5KwiCls2a5KF2pHt3VbrG=w60-h60-p-l90-rj",
            artist.artist?.images?.firstOrNull()?.url
        )
    }
}