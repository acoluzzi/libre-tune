package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.model.TopResult
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue


@RunWith(Enclosed::class)
@DisplayName("Search")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchExtractorTests {
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
    fun `scrapeSearch should correctly parse Iron Maiden search HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("search_iron_maiden.html")

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
        val result = scraper.search("any_id")


        assertEquals(4, result?.topResults?.size)
        assertTrue(result?.topResults?.get(0) is TopResult.ArtistResult)
        val artistResult = result.topResults.get(0) as TopResult.ArtistResult
        assertEquals("Iron Maiden", artistResult.artist?.name)
        assertEquals("UC0zbzp6x7zR8u0LhanNWFyw", artistResult.artist?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/nhiZLMxjoxcIOcjrMr_Es_0ARMq0iCMkcfMivwQZrF5UL0kJwUthJIAH6dnQstu3MbmyyPGX5RazQ9E=w60-h60-p-l90-rj",
            artistResult.artist?.images?.firstOrNull()?.url
        )


        assertTrue(result?.topResults?.get(1) is TopResult.SongResult)
        val topSong = result.topResults.get(1) as TopResult.SongResult
        assertEquals("Fear of the Dark", topSong.song?.title)
        assertEquals("bePCRKGUwAY", topSong.song?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/oJwgqSS3BqNI7lLB43eOkiiKCfgMFdFucJ5yI4XDGYovcbim9TrKYMg2t4ciHQ1jjbq0re3fpgBZrz1s=w60-h60-l90-rj",
            topSong.song?.images?.firstOrNull()?.url
        )

        assertEquals(3, result?.albums?.size)
        assertEquals("Iron Maiden", result?.albums?.get(0)?.name)
        assertEquals("MPREb_tBWoJuHumZq", result?.albums?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/8VjfILOF4PNwAB3yE8C8Iw27EdezzMAbsjdcie_-UJ0S55wcNrLwKUhuAQ8BLY2M97Y8uYeOOSN1HsIr=w60-h60-l90-rj",
            result?.albums?.get(0)?.images?.firstOrNull()?.url
        )
        assertEquals("Iron Maiden", result?.albums?.get(0)?.artists?.firstOrNull()?.name)
        assertEquals("UC0zbzp6x7zR8u0LhanNWFyw", result?.albums?.get(0)?.artists?.firstOrNull()?.id)


        assertEquals(5, result?.songs?.size)

        var songIndex = 0

        assertEquals("Empire of the Clouds", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("9CWTig2kBKE", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/ptg2nzSk0d7yYCY6mmfIX0pwLWyHIMSVd9Z9ArCBFYpJ0hcDyvVY4iP9c9SjNrxGwV8HgkTHgfpQYKUW=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )

        assertEquals(
            "The Book of Souls",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_vdA6ZG7XgnM",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals("Phantom of the Opera", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("p2ct4xXak24", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/8VjfILOF4PNwAB3yE8C8Iw27EdezzMAbsjdcie_-UJ0S55wcNrLwKUhuAQ8BLY2M97Y8uYeOOSN1HsIr=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_tBWoJuHumZq",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals("Powerslave", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("Mw-o_cSdqmI", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/0DbjxIGQRRzZsSWJU3K0ig7NoyHsI0H1LQrVmgQB3rZGzRFKBG_6yUhc8ARoD9IyW_m3pIdILnF5oY_2=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Powerslave",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_SW8KWngfQ0p",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals("Rime of the Ancient Mariner", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("OSDZj_jh5cE", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/0DbjxIGQRRzZsSWJU3K0ig7NoyHsI0H1LQrVmgQB3rZGzRFKBG_6yUhc8ARoD9IyW_m3pIdILnF5oY_2=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Powerslave",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_SW8KWngfQ0p",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals("Seventh Son of a Seventh Son", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("ZjphaXXEU9o", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/ij4AI6gN0itrpLrvHt43bhFRJT46xsQHPu0NxGwEy4j662zjs-KQyoEKQn0Co5YVeEEIdzFAb_8huJ8=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Seventh Son of a Seventh Son",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_zlq9N8ZrxCS",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )



        assertEquals(3, result?.artists?.size)
        assertEquals("Iron Maidnem (tribute to Iron Maiden)", result?.artists?.get(0)?.name)
        assertEquals("UCRtwQY5pkAmbbi3IduOds6g", result?.artists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/H7hOzR2ASOPafYoQ6r4gLlW-7MZWwWV3cLIlksvcCzVFzBmOr0h_c5hkVZlgb-5R79UVM_9pd6fNOk4=w60-h60-l90-rj",
            result?.artists?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(1, result?.playlists?.size)
        assertEquals("Presenting Iron Maiden", result?.playlists?.get(0)?.name)
        assertEquals(
            "VLRDCLAK5uy_kXf-8pT9GiXhfzq036-by6qhgc2Ux4XjA",
            result?.playlists?.get(0)?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/AHWQuSpnVbAIN7NLMsDqI7DoahzJ6464LdZgSrifOiiwfJPZBHnp28eVnZjfOKpn__xKw458N2uqzxQP=w60-h60-l90-rj",
            result?.playlists?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(3, result?.communityPlaylists?.size)
        assertEquals(
            "Iron Maiden - Ullevi 2005 Remastered",
            result?.communityPlaylists?.get(0)?.name
        )
        assertEquals(
            "VLPLmCYMRaayLcwDwvrOVHtn2wMLM5Cbarot",
            result?.communityPlaylists?.get(0)?.id
        )
        assertEquals(
            "https://i.ytimg.com/vi/PMm5b7cifXA/sddefault.jpg?sqp=-oaymwEWCJADEOEBIAQqCghqEJQEGHgg6AJIWg&rs=AMzJL3l0HSfCFm4Pb0u2QVHngrpKBDMkkQ",
            result?.communityPlaylists?.get(0)?.images?.firstOrNull()?.url
        )


    }


    @Test
    fun `scrapeSearch should correctly parse Fear Of The Dark search HTML`() = runTest {
        // Arrange: Create a fake HTML response
        val fakeHtml = TestUtil.readFileFromResources("search_fear_of_the_dark.html")

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
        val result = scraper.search("any_id")


        assertEquals(1, result?.topResults?.size)
        assertTrue(result?.topResults?.get(0) is TopResult.AlbumResult)
        val topResult = result.topResults.get(0) as TopResult.AlbumResult
        assertEquals("Fear of the Dark", topResult.album?.name)
        assertEquals("MPREb_2ER16Pnctup", topResult.album?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/oJwgqSS3BqNI7lLB43eOkiiKCfgMFdFucJ5yI4XDGYovcbim9TrKYMg2t4ciHQ1jjbq0re3fpgBZrz1s=w60-h60-l90-rj",
            topResult.album?.images?.firstOrNull()?.url
        )
        assertEquals("Iron Maiden", topResult.album?.artists?.firstOrNull()?.name)
        assertEquals("UC0zbzp6x7zR8u0LhanNWFyw", topResult.album?.artists?.firstOrNull()?.id)



        assertEquals(3, result?.albums?.size)
        assertEquals("Fear of the Dark", result?.albums?.get(0)?.name)
        assertEquals("MPREb_SLsVD0xNytd", result?.albums?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/cr1tTd79Ve-QbDgRLAor8Fk3iNpY1eyjOLRsF8YpsgFORZOIATEiGuG5NWdF0RmKPMgF0w5-Ti-4INLNfA=w60-h60-l90-rj",
            result?.albums?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals("Spruced Archaic", result?.albums?.get(0)?.artists?.firstOrNull()?.name)
        assertEquals("UCAEhlcsRgDQAhnEPfPCfPkQ", result?.albums?.get(0)?.artists?.firstOrNull()?.id)



        assertEquals(3, result?.songs?.size)

        var songIndex = 0

        assertEquals("Fear of the Dark", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("bePCRKGUwAY", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/oJwgqSS3BqNI7lLB43eOkiiKCfgMFdFucJ5yI4XDGYovcbim9TrKYMg2t4ciHQ1jjbq0re3fpgBZrz1s=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )

        assertEquals(
            "Fear of the Dark",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_2ER16Pnctup",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals(
            "Scared of the Dark (feat. XXXTENTACION)",
            result?.songs?.get(songIndex)?.title
        )
        assertEquals(
            2,
            result?.songs?.get(songIndex)?.artists?.size
        )
        assertEquals(
            "Lil Wayne",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC4IAZ3dowcXyvVYBx4hucSQ",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals(
            "Ty Dolla \$ign",
            result?.songs?.get(songIndex)?.artists?.get(1)?.name
        )
        assertEquals(
            "UC_Wl1icJ-lfoz75E99PSVrQ",
            result?.songs?.get(songIndex)?.artists?.get(1)?.id
        )
        assertEquals("zVlFkFmk_NM", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/IBwK4LOs4sO-Cd-_5YO4XC2B2N1hvyBvKfr60tT_ljIuwmOuVWWW2NypbcvzKgScVAbRsWEYbmsjfcm2cw=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Spider-Man: Into the Spider-Verse (Soundtrack From & Inspired by the Motion Picture)",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_XLbJEEU4CpR",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++


        assertEquals("Fear of the Dark (Live '01)", result?.songs?.get(songIndex)?.title)
        assertEquals(
            "Iron Maiden",
            result?.songs?.get(songIndex)?.artists?.get(0)?.name
        )
        assertEquals(
            "UC0zbzp6x7zR8u0LhanNWFyw",
            result?.songs?.get(songIndex)?.artists?.get(0)?.id
        )
        assertEquals("DOlsDVPXuPQ", result?.songs?.get(songIndex)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/2ND7QE7oafLFZW4Y711TNPrREFUn8tLKla4kLVUAzk8r9-f_nvlWrKv1aAE3v4IAAbqU_WUm1bR2401P=w60-h60-l90-rj",
            result?.songs?.get(songIndex)?.images?.firstOrNull()?.url
        )
        assertEquals(
            "Rock in Rio (Live)",
            result?.songs?.get(songIndex)?.album?.name
        )
        assertEquals(
            "MPREb_rYwPTaKSpB4",
            result?.songs?.get(songIndex)?.album?.id
        )
        assertEquals(
            0,
            result?.songs?.get(songIndex)?.album?.images?.size
        )
        songIndex++



        assertEquals(1, result?.artists?.size)
        assertEquals("Iron Maiden", result?.artists?.get(0)?.name)
        assertEquals("UC0zbzp6x7zR8u0LhanNWFyw", result?.artists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/nhiZLMxjoxcIOcjrMr_Es_0ARMq0iCMkcfMivwQZrF5UL0kJwUthJIAH6dnQstu3MbmyyPGX5RazQ9E=w60-h60-p-l90-rj",
            result?.artists?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(0, result?.playlists?.size)

        assertEquals(3, result?.communityPlaylists?.size)
        assertEquals(
            "Iron Maiden-Fear of the Dark",
            result?.communityPlaylists?.get(0)?.name
        )
        assertEquals(
            "VLPL8t_WbLHlEHN2oASbsGdAxyd6-PS1IoS5",
            result?.communityPlaylists?.get(0)?.id
        )
        assertEquals(
            "https://i.ytimg.com/vi/2DiCRvf9yJc/sddefault.jpg?sqp=-oaymwEWCJADEOEBIAQqCghqEJQEGHgg6AJIWg&rs=AMzJL3mHu2Qe0cFKrwuO31jBAvZlK1Z74Q",
            result?.communityPlaylists?.get(0)?.images?.firstOrNull()?.url
        )


    }


}