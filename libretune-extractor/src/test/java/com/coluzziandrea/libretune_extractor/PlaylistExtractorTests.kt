package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.util.TestUtil
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@DisplayName("Playlist")
@RunWith(Enclosed::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaylistPageScrapingTests {
    @Mock
    private lateinit var mockClient: OkHttpClient

    @Mock
    private lateinit var mockCall: Call

    private lateinit var scraper: LibreTuneExtractor

    @BeforeAll
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        scraper = LibreTuneExtractor(mockClient)
    }

    @Test
    fun `scrapePlaylist should correctly parse sanremo2025 HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("sanremo2025.html")

        val responseBody = ResponseBody.create("text/html".toMediaTypeOrNull(), fakeHtml)
        val response = Response.Builder()
            .request(Request.Builder().url("http://googleusercontent.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

        // Tell the mock client what to do when a call is made
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(response)

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
