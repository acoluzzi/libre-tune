package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.util.TestUtil
import com.coluzziandrea.libretune_extractor.util.provideMockClient
import kotlinx.coroutines.test.runTest
import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(Enclosed::class)
class DiscographyExtractorTests {

    @Test
    fun `scrapeSearch should correctly parse eminem albums`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("moreAlbumEminem.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val result = scraper.discography("any_id")


        assertEquals(22, result?.albums?.size)

        val firstAlbum = result?.albums?.firstOrNull()
        assertEquals("STANS (The Official Soundtrack)", firstAlbum?.name)
        assertEquals("MPREb_DmBhgAVua1o", firstAlbum?.id)
        assertEquals(2025, firstAlbum?.releaseYear)
        assertEquals(2, firstAlbum?.images?.size)
        assertEquals(
            "https://lh3.googleusercontent.com/xIN67aenJGjjn7dl1SugpJFocYQiXMiYoFfaukVxJJ7NGhLZhm18n8Pr2-k7m8wr9-wJy5BdpMloqzS8=w226-h226-l90-rj",
            firstAlbum?.images?.firstOrNull()?.url
        )

    }
}