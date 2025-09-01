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

@DisplayName("Album")
@RunWith(Enclosed::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlbumPageScrapingTests {

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
    fun `scrapePlaylist should correctly parse abbeyRoad HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("abbey_road.html")

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
        assertEquals("Abbey Road (Super Deluxe Edition)", playlistDetails?.name)
        assertEquals(
            "https://lh3.googleusercontent.com/g8bzAg2zxvdnm7ismLMYLA9-9azb4y6VP2uOF56A2G2rpsqLHT6mrJWXRKq_VttXQZ-o-jmVgTFIVgdj=w60-h60-l90-rj",
            playlistDetails?.images?.firstOrNull()?.url
        )

        assertNotNull(playlistDetails?.songs)
        assertEquals(40, playlistDetails?.songs?.size)
        assertEquals("Come Together (2019 Mix)", playlistDetails?.songs?.get(0)?.title)
        assertEquals("45cYwDMibGo", playlistDetails?.songs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_lqcFZTOPHGwcnP0nYMzNuY0IES0fl7Fe4",
            playlistDetails?.songs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/g8bzAg2zxvdnm7ismLMYLA9-9azb4y6VP2uOF56A2G2rpsqLHT6mrJWXRKq_VttXQZ-o-jmVgTFIVgdj=w60-h60-l90-rj",
            playlistDetails?.songs?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, playlistDetails?.relatedPlaylists?.size)
        assertEquals(
            "Sounds Of Silence",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_WzySvZJyDsg", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/n2QV30WE3MKk1E2-XqiUBDz9v7MTiWQF3t2HmbndDBGtnFu41pNehddWzhwX8BZlDbsCGGvz179HnEQ=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }

    @Test
    fun `scrapePlaylist should correctly parse deathMagnetic HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("death_magnetic.html")

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
        assertEquals("Death Magnetic", playlistDetails?.name)
        assertEquals(
            "https://lh3.googleusercontent.com/jKSy3N15Nd2vF0OG4m10y4A-GgN94CJQIyseGZ0HjJIDUL9dqfY1SI4mqOuJkuUdmaOQ-HRI9q_9BXo=w60-h60-l90-rj",
            playlistDetails?.images?.firstOrNull()?.url
        )

        assertNotNull(playlistDetails?.songs)
        assertEquals(10, playlistDetails?.songs?.size)
        assertEquals("That Was Just Your Life", playlistDetails?.songs?.get(0)?.title)
        assertEquals("VBKtoY8TucA", playlistDetails?.songs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_lIZirsnSbg41CX1xYgibpRhMlZWOwb1LM",
            playlistDetails?.songs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/jKSy3N15Nd2vF0OG4m10y4A-GgN94CJQIyseGZ0HjJIDUL9dqfY1SI4mqOuJkuUdmaOQ-HRI9q_9BXo=w60-h60-l90-rj",
            playlistDetails?.songs?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, playlistDetails?.relatedPlaylists?.size)
        assertEquals(
            "Overkill (Exclusive Version)",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_ZGG3JZ6eAEz", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/8wtjuBicAha4rscwnt2vM7fvywMf2MyrTLDSvYpGA_UQCHKdIQOSX4PLpX2lxN-57JCH3ljp9b0H02oP=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }


    @Test
    fun `scrapePlaylist should correctly parse shotDownInTheBigEasy HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("shot_down_in_the_big_easy.html")

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
        assertEquals("Shot Down In The Big Easy", playlistDetails?.name)
        assertEquals(
            "https://lh3.googleusercontent.com/oj-0oUnP5pwhlbGFvX8KJ9VOQjV-I0RErIJ-Fz2XeLXZ0llgSMudPUukkh3pyJUUAs4y-h_pyEreMQ0o=w60-h60-l90-rj",
            playlistDetails?.images?.firstOrNull()?.url
        )

        assertNotNull(playlistDetails?.songs)
        assertEquals(25, playlistDetails?.songs?.size)
        assertEquals("Back In Black", playlistDetails?.songs?.get(0)?.title)
        assertEquals("oH751DemK_8", playlistDetails?.songs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_kUh4caU62Brx6Op4EEtRY_z6ksFPmDuEY",
            playlistDetails?.songs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/oj-0oUnP5pwhlbGFvX8KJ9VOQjV-I0RErIJ-Fz2XeLXZ0llgSMudPUukkh3pyJUUAs4y-h_pyEreMQ0o=w60-h60-l90-rj",
            playlistDetails?.songs?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, playlistDetails?.relatedPlaylists?.size)
        assertEquals(
            "Ac/Dc Medley: Highway to Hell / Touch Too Much / Back in Black / Shot Down in Flames / Thunderstruck / You Shook Me All Night Long / Sin City / She's Got Balls / Dirty Deeds Done Dirt Cheap",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_US5vRZVKy5s", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/92FWou-vY0wcP53mDtjlW7RjgATwfdEFzkLWvuHZ2yjyCnKxI-4lbgyn2ccedQG4Nb8f6N3sIYcUtpX4=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }
}

