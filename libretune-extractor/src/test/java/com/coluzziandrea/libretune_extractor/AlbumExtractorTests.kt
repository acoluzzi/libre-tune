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

@DisplayName("Album")
@RunWith(Enclosed::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlbumPageScrapingTests {


    @Test
    fun `scrapePlaylist should correctly parse abbeyRoad HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("abbeyRoad.json")))

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
            "Let It Be (Super Deluxe)",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_zBKX8qwlKte", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/0uSK3j19kosq8SmrnZZ_mlw3kL6ZWFcLRgt0cqhACJcA6cEfLgCscIllVfF-LjkuV3zhuYG6MSFih6PdMw=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }

    @Test
    fun `scrapePlaylist should correctly parse deathMagnetic HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("deathMagnetic.json")))


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
            "Awake",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_TDaJ1HErJsN", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/cuAmsZeqgsDjEb0DIe0UyYCGXVm-kIMoGTXF-4y4B8eiDRjpr7i4lJzKNDyxTIiC4_Zp_Y-82h1bxe5N=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }


    @Test
    fun `scrapePlaylist should correctly parse shotDownInTheBigEasy HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("shotdowninthebigeasy.json")))


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
            "The Early Years",
            playlistDetails?.relatedPlaylists?.get(0)?.name
        )
        assertEquals("MPREb_HEOEUG7Pe8n", playlistDetails?.relatedPlaylists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/JLdWzTgNqfFoLaWkBMmowC-f6WsfQyVyUO8vv3FLe5M_5zA-aKlelDQte_O_Wfc2UplymrUlIs_zRe6T=w226-h226-l90-rj",
            playlistDetails?.relatedPlaylists?.get(0)?.images?.firstOrNull()?.url
        )

    }
}

