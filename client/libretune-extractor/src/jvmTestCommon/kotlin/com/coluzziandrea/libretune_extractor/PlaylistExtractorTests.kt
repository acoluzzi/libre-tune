package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.util.TestUtil
import com.coluzziandrea.libretune_extractor.util.provideMockClient
import kotlinx.coroutines.test.runTest
import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith

@DisplayName("Playlist")
@RunWith(Enclosed::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaylistPageScrapingTests {


    @Test
    fun `scrapePlaylist should correctly parse sanremo2025 HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("sanremo2025.json")))


        // Act: Call the method with any channel ID (it won't be used)
        val playlistDetails = scraper.playlist("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(playlistDetails)
        assertEquals("Sanremo 2025", playlistDetails?.name)
        assertEquals(
            "https://lh3.googleusercontent.com/Oo3AzftPmxArlhTwmbsQxhPSTWalRorPF3pjf7r2IRxfIAsYFqvB1JDd15VYcabqa41sn7fIkWzpbeg=w60-h60-l90-rj",
            playlistDetails?.images?.firstOrNull()?.url
        )

        assertNotNull(playlistDetails?.songs)
        assertEquals(29, playlistDetails?.songs?.size)
        assertEquals("Incoscienti Giovani", playlistDetails?.songs?.get(0)?.title)
        assertEquals("Achille Lauro", playlistDetails?.songs?.get(0)?.artists?.get(0)?.name)
        assertEquals("qU-KFzHN1wM", playlistDetails?.songs?.get(0)?.id)
        assertEquals(
            "RDCLAK5uy_nHn0clIvDJ3tfaKC9kFs9x1Fy1hR65jUo",
            playlistDetails?.songs?.get(0)?.playlistId
        )
        assertEquals(
            "https://i.ytimg.com/vi/qU-KFzHN1wM/sddefault.jpg?sqp=-oaymwEWCJADEOEBIAQqCghqEJQEGHgg6AJIWg&rs=AMzJL3kSoYGxksHHrJLF8SsJ_seJHbOsrA",
            playlistDetails?.songs?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals("La Mia Parola (feat. Tormento)", playlistDetails?.songs?.get(27)?.title)
        assertEquals("Shablo", playlistDetails?.songs?.get(27)?.artists?.get(0)?.name)
        assertEquals("Guè", playlistDetails?.songs?.get(27)?.artists?.get(1)?.name)
        assertEquals("Joshua", playlistDetails?.songs?.get(27)?.artists?.get(2)?.name)



        assertEquals(0, playlistDetails?.relatedPlaylists?.size)

    }
}
