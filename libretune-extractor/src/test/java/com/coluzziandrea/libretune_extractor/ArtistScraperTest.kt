package com.coluzziandrea.libretune_extractor

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.InputStreamReader


class ArtistScraperTest {
    @Mock
    private lateinit var mockClient: OkHttpClient

    @Mock
    private lateinit var mockCall: Call

    private lateinit var scraper: ArtistScraper

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        scraper = ArtistScraper(mockClient)
    }

    // Helper function to read a file from the test/resources folder
    private fun readFileFromResources(fileName: String): String {
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
        val reader = InputStreamReader(inputStream)
        return reader.readText()
    }

    @Test
    fun `scrapeArtistPage should correctly parse beatles HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = readFileFromResources("beatles.html")

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
        val artistDetails = scraper.scrapeArtistPage("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(artistDetails)
        assertEquals("The Beatles", artistDetails?.name)
        assert(artistDetails?.description?.contains("It did all happen. The whole wonderful thing did happen, a long time ago, on the Mersey, on the") == true)
        assertEquals(
            "https://lh3.googleusercontent.com/z8KZsHNKS-O1qYVyKlSErT_RLMSMwVht89USvSdFAd0EoRlBOppi9DOdRkv609Ye_tfq_Wp8WwhVJbw=w544-h544-p-l90-rj",
            artistDetails?.bannerUrl
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Let It Be (Remastered 2009)", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("QDYfEBY9NM4", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_mSSvmI1EpoPDI0BbUg1bPCOc6_pF8150Q",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/octdAIhLRBSYd5JKOeTsF5zNhQ4C0L3JtOnjUYPvHLtJaxXr68NVW8gUfsE05aarfaDmZe_ibrVMxo-y4g=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.imageUrl
        )

    }

    @Test
    fun `scrapeArtistPage should correctly parse acdc HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = readFileFromResources("acdc.html")

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
        val artistDetails = scraper.scrapeArtistPage("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(artistDetails)
        assertEquals("AC/DC", artistDetails?.name)
        assertEquals(
            "AC/DC are an Australian rock band formed in Sydney in 1973. Their music has been variously described as hard rock, blues rock and heavy metal, although the band calls it simply \"rock and roll\". They are cited as a formative influence on the new wave of British heavy metal bands. The band was inducted into the Rock and Roll Hall of Fame in 2003 and have sold over 200 million records worldwide, making them one of the best-selling artists of all time.\n" +
                    "AC/DC were founded by brothers Angus and Malcolm Young, with Colin Burgess, Larry Van Kriedt and Dave Evans. They underwent several line-up changes before releasing their debut Australasian-only album, High Voltage. Membership stabilised after the release of Let There Be Rock, with the Young brothers, Phil Rudd on drums, Cliff Williams on bass guitar and Bon Scott on lead vocals. Seven months after the release of Highway to Hell, Scott died of alcohol poisoning and English singer Brian Johnson was then recruited as their new frontman. Their first album with Johnson, Back in Black, dedicated to Scott's memory, became the second best-selling album of all time.",
            artistDetails?.description
        )
        assertEquals(
            "https://lh3.googleusercontent.com/IlNp_o9GKakp7qtaDAKaDxpW29qtP8sjqQXPcBQ9uAdOUU3AnJdB85xLRiYIGT3FlCmobm6oiMQX4GU=w544-h544-p-l90-rj",
            artistDetails?.bannerUrl
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Thunderstruck", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("lhg9bYNLvOg", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_lVoh11X5c3o6PR2nO88388e9jdmc7deac",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/DV_ebk0MH4HMLfn2CxeH6Thjf9OSs1Q6FhZulHE6KIyoPWYT6rh2FOZIERUFLRKg7yxpPqwIBE31D5ETkg=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.imageUrl
        )

    }


    @Test
    fun `scrapeArtistPage should correctly parse metallica HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = readFileFromResources("metallica.html")

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
        val artistDetails = scraper.scrapeArtistPage("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(artistDetails)
        assertEquals("Metallica", artistDetails?.name)
        assertEquals(
            "Metallica formed in 1981 by drummer Lars Ulrich and guitarist and vocalist James Hetfield and has become one of the most influential and commercially successful rock bands in history, having sold 110 million albums worldwide while playing to millions of fans on literally all seven continents. They have scored several multi-platinum albums, including 1991’s Metallica (commonly referred to as The Black Album), with sales of nearly 17 million copies in the United States alone, making it the best-selling album in the history of Soundscan. Metallica has also garnered numerous awards and accolades, including nine Grammy Awards, two American Music Awards, and multiple MTV Video Music Awards, and were inducted into the Rock and Roll Hall of Fame and Museum in 2009.  In December 2013, Metallica made history when they performed a rare concert in Antarctica, becoming the first act to ever play all seven continents all within a year, and earning themselves a spot in the Guinness Book of World Records.",
            artistDetails?.description
        )
        assertEquals(
            "https://lh3.googleusercontent.com/a-/ALV-UjXGomQMrlDtBWEXl_Ugjti3oL8lSQEH_qc4MG3Kmzi-ppE25srs=w544-h544-l90-rj",
            artistDetails?.bannerUrl
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Nothing Else Matters", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("pTYIf2pkxzQ", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_miOHTfPNlwsbuoYYtAeefJHDm3Qcv-ebQ",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/2SJUS7YtuaGIBU8-0lFxMi_T6Ned9JjM3GvZJr3JJIPNQxwXSa8hIbSOSxl1tRaPHnrLDVfJBJBvuqg=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.imageUrl
        )

    }
}