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

@DisplayName("Artist")
@RunWith(Enclosed::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArtistPageScrapingTests {


    @Test
    fun `scrapeArtistPage should correctly parse beatles HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("theBeatles.json")))


        // Act: Call the method with any channel ID (it won't be used)
        val artistDetails = scraper.artist("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(artistDetails)
        assertEquals(
            "The Beatles",
            artistDetails?.name
        )
        assert(artistDetails?.description?.contains("We didn't dream it... though it came out of John's dream of") == true)
        assertEquals(
            "https://lh3.googleusercontent.com/z8KZsHNKS-O1qYVyKlSErT_RLMSMwVht89USvSdFAd0EoRlBOppi9DOdRkv609Ye_tfq_Wp8WwhVJbw=w544-h544-p-l90-rj",
            artistDetails?.images?.first()?.url
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Let It Be (Remastered 2009)", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("The Beatles", artistDetails?.topSongs?.get(0)?.artists?.get(0)?.name)
        assertEquals("QDYfEBY9NM4", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_mSSvmI1EpoPDI0BbUg1bPCOc6_pF8150Q",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/octdAIhLRBSYd5JKOeTsF5zNhQ4C0L3JtOnjUYPvHLtJaxXr68NVW8gUfsE05aarfaDmZe_ibrVMxo-y4g=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, artistDetails?.albums?.size)
        assertEquals(
            "Beatles '64 (Music from the Disney+ Documentary)",
            artistDetails?.albums?.get(0)?.name
        )
        assertEquals("MPREb_OLtz6K1cjET", artistDetails?.albums?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/bMY8zm6aijac0ykQxvifCWOvtIF9IaVPhTD3IW5nIuwghU3QtvmRPBcsRIqdnB7H2VIWKs5J7OZ9wZff=w226-h226-l90-rj",
            artistDetails?.albums?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, artistDetails?.similarArtists?.size)
        assertEquals("Traveling Wilburys", artistDetails?.similarArtists?.get(0)?.name)
        assertEquals("UC2zPxEtkJD0hh8cpXq2Rl9A", artistDetails?.similarArtists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/dpALL4GjEupfSup1LkCzrolcH9YZFOTVzNcBgfRADjWI7FAusKNYBd1yJpDq82LoTkD7CxtERYvlxQk=w226-h226-p-l90-rj",
            artistDetails?.similarArtists?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(2, artistDetails?.singlesAndEp?.size)
        assertEquals("Free As A Bird (2025 Mix)", artistDetails?.singlesAndEp?.get(0)?.name)
        assertEquals("MPREb_RFwSD0tp3ZA", artistDetails?.singlesAndEp?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/O7-pu1WloTc_ortWcFfH-u-9t1xMsgYatQKF130s_YLtlBmh5EASF23BYFbptqfN_uF0I8QSnoQhAco9=w226-h226-l90-rj",
            artistDetails?.singlesAndEp?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.featuring?.size)
        assertEquals("Presenting The Beatles", artistDetails?.featuring?.get(0)?.name)
        assertEquals(
            "VLRDCLAK5uy_nhetVOKK6_8JKmkKrLcfiXZAVWhNEAPC4",
            artistDetails?.featuring?.get(0)?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/jGf8Fhs5mcuymBGeSNt1XgIEo0yC9sBN7hwEBQ5x590ZO_dHu0XVtzTVBD_OkY5tYcwTywu9A9IZ4bI=w226-h226-l90-rj",
            artistDetails?.featuring?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.playlists?.size)
        assertEquals(
            "The Beatles - Beatles 100 (Official Playlist)",
            artistDetails?.playlists?.get(0)?.name
        )
        assertEquals(
            "VLPL0jp-uZ7a4g9FQWW5R_u0pz4yzV4RiOXu",
            artistDetails?.playlists?.get(0)?.id
        )
        assertEquals(
            "https://yt3.ggpht.com/YBt8Xst5N_I35MAUzdZwh_5ltgdlU7uxsojLYVqUIYUV8GTuKSDZ0K5O1AaFIuH3BZ20b4KbM5U=s192",
            artistDetails?.playlists?.get(0)?.images?.firstOrNull()?.url
        )

    }

    @Test
    fun `scrapeArtistPage should correctly parse acdc HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("acdc.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val artistDetails = scraper.artist("any_id")

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
            artistDetails?.images?.first()?.url
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Thunderstruck", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("AC/DC", artistDetails?.topSongs?.get(0)?.artists?.get(0)?.name)
        assertEquals("lhg9bYNLvOg", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_lVoh11X5c3o6PR2nO88388e9jdmc7deac",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/DV_ebk0MH4HMLfn2CxeH6Thjf9OSs1Q6FhZulHE6KIyoPWYT6rh2FOZIERUFLRKg7yxpPqwIBE31D5ETkg=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.albums?.size)
        assertEquals("Shot Down In The Big Easy", artistDetails?.albums?.get(0)?.name)
        assertEquals("MPREb_mInwWNWkfbx", artistDetails?.albums?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/oj-0oUnP5pwhlbGFvX8KJ9VOQjV-I0RErIJ-Fz2XeLXZ0llgSMudPUukkh3pyJUUAs4y-h_pyEreMQ0o=w226-h226-l90-rj",
            artistDetails?.albums?.get(0)?.images?.firstOrNull()?.url
        )



        assertEquals(10, artistDetails?.similarArtists?.size)
        assertEquals("Twisted Sister", artistDetails?.similarArtists?.get(0)?.name)
        assertEquals("UC8-GQfF2Xod92t8kiUe2ajA", artistDetails?.similarArtists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/H4nruUcEUJ3E4__3Ern36a0duiuE2hzwrkOYEsfJFezvvREHbP5_RQf2OcfiOIh52qpfQqyn9Xb7ibeU=w226-h226-p-l90-rj",
            artistDetails?.similarArtists?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, artistDetails?.singlesAndEp?.size)
        assertEquals("Realize", artistDetails?.singlesAndEp?.get(0)?.name)
        assertEquals("MPREb_dIdasNmkwkk", artistDetails?.singlesAndEp?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/l7bMTqeHjXCWuSRPZ_5WRWJvunpCChVSrh9XeB4Zb4gnigwIvT9mVLuIf8g4Dd8ejFUkuUFxtgkSqhkw=w226-h226-l90-rj",
            artistDetails?.singlesAndEp?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.featuring?.size)
        assertEquals("Presenting AC/DC", artistDetails?.featuring?.get(0)?.name)
        assertEquals(
            "VLRDCLAK5uy_kur4UyyjC-_NPV1kf86ZTHn830aFzcnm8",
            artistDetails?.featuring?.get(0)?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/NiGNADW5KMheBFYHGqosaoAB0WqvjJFcI32fGh2aktIKaN_Gt_CTBAHOZFsl954m_VPqmRiC95qQVQ=w226-h226-l90-rj",
            artistDetails?.featuring?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.playlists?.size)
        assertEquals("AC/DC - POWER UP", artistDetails?.playlists?.get(0)?.name)
        assertEquals(
            "VLPLx1MDbsLNfVReIJ-ljqWW4vACm6alJgbF",
            artistDetails?.playlists?.get(0)?.id
        )
        assertEquals(
            "https://yt3.googleusercontent.com/mLsHmia72pA53gOe97h7ka59SVdHm7-gXJpY24f4oWq2hOC_wdRO5cOp7oEKe6Zl9xFSdhu82PQ=s192",
            artistDetails?.playlists?.get(0)?.images?.firstOrNull()?.url
        )
    }


    @Test
    fun `scrapeArtistPage should correctly parse metallica HTML`() = runTest {
        val scraper =
            LibreTuneExtractor(provideMockClient(TestUtil.readFileFromResources("metallica.json")))

        // Act: Call the method with any channel ID (it won't be used)
        val artistDetails = scraper.artist("any_id")

        // Assert: Check if your parsing logic worked on the FAKE HTML
        assertNotNull(artistDetails)
        assertEquals("Metallica", artistDetails?.name)
        assertEquals(
            "Metallica formed in 1981 by drummer Lars Ulrich and guitarist and vocalist James Hetfield and has become one of the most influential and commercially successful rock bands in history, having sold 110 million albums worldwide while playing to millions of fans on literally all seven continents. They have scored several multi-platinum albums, including 1991’s Metallica (commonly referred to as The Black Album), with sales of nearly 17 million copies in the United States alone, making it the best-selling album in the history of Soundscan. Metallica has also garnered numerous awards and accolades, including nine Grammy Awards, two American Music Awards, and multiple MTV Video Music Awards, and were inducted into the Rock and Roll Hall of Fame and Museum in 2009.  In December 2013, Metallica made history when they performed a rare concert in Antarctica, becoming the first act to ever play all seven continents all within a year, and earning themselves a spot in the Guinness Book of World Records.",
            artistDetails?.description
        )
        assertEquals(
            "https://lh3.googleusercontent.com/a-/ALV-UjXGomQMrlDtBWEXl_Ugjti3oL8lSQEH_qc4MG3Kmzi-ppE25srs=w544-h544-l90-rj",
            artistDetails?.images?.firstOrNull()?.url
        )

        assertNotNull(artistDetails?.topSongs)
        assert(artistDetails?.topSongs?.isNotEmpty() == true)
        assertEquals(5, artistDetails?.topSongs?.size)
        assertEquals("Nothing Else Matters", artistDetails?.topSongs?.get(0)?.title)
        assertEquals("Metallica", artistDetails?.topSongs?.get(0)?.artists?.get(0)?.name)
        assertEquals("pTYIf2pkxzQ", artistDetails?.topSongs?.get(0)?.id)
        assertEquals(
            "OLAK5uy_miOHTfPNlwsbuoYYtAeefJHDm3Qcv-ebQ",
            artistDetails?.topSongs?.get(0)?.playlistId
        )
        assertEquals(
            "https://lh3.googleusercontent.com/2SJUS7YtuaGIBU8-0lFxMi_T6Ned9JjM3GvZJr3JJIPNQxwXSa8hIbSOSxl1tRaPHnrLDVfJBJBvuqg=w60-h60-l90-rj",
            artistDetails?.topSongs?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.albums?.size)
        assertEquals("Under The Covers", artistDetails?.albums?.get(0)?.name)
        assertEquals("MPREb_Sc6OpME3TWu", artistDetails?.albums?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/vCwdAeNhB0HRSg0vzY3RwSAQwzmaiU_dA7xQ1Fq-31ffXF3FKbVMpgeFy8Ws5KqjSBADbUEDx4vNpLjL=w226-h226-l90-rj",
            artistDetails?.albums?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, artistDetails?.similarArtists?.size)
        assertEquals("Ozzy Osbourne", artistDetails?.similarArtists?.get(0)?.name)
        assertEquals("UC3oY0sESMqxZaAy8nRcVxQQ", artistDetails?.similarArtists?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/xbR9cvRRy6-MOnZbVbpTDc4m706kpoj7yiM-cRxXL-G_5tvgsXh50AbSQTHcnPG0DbI1J543tduScbU=w226-h226-p-l90-rj",
            artistDetails?.similarArtists?.get(0)?.images?.firstOrNull()?.url
        )


        assertEquals(10, artistDetails?.singlesAndEp?.size)
        assertEquals("72 Seasons", artistDetails?.singlesAndEp?.get(0)?.name)
        assertEquals("MPREb_49wzEEqwX0i", artistDetails?.singlesAndEp?.get(0)?.id)
        assertEquals(
            "https://lh3.googleusercontent.com/ZY4ZZepXmrPnEjFHPDcNXCurK4BXZxLiU_XNFK8IhS5dGmj9kfnSSQWqsD6aciqg4vW-tSGWs20veseQ=w226-h226-l90-rj",
            artistDetails?.singlesAndEp?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.featuring?.size)
        assertEquals("Presenting Metallica", artistDetails?.featuring?.get(0)?.name)
        assertEquals(
            "VLRDCLAK5uy_kUx_HchTY26TcozNcRAi-xmp2mISNEvTU",
            artistDetails?.featuring?.get(0)?.id
        )
        assertEquals(
            "https://lh3.googleusercontent.com/YoQ-YE_Ipl6mYG1XI5QaydMSvFm07JVLx225idwKk-x0j6aOJlBViGLp62fscR2KWSBkeS3SOgVAn98=w226-h226-l90-rj",
            artistDetails?.featuring?.get(0)?.images?.firstOrNull()?.url
        )

        assertEquals(10, artistDetails?.playlists?.size)
        assertEquals(
            "Metallica's Official Music Videos",
            artistDetails?.playlists?.get(0)?.name
        )
        assertEquals("VLPL2D4A44B959D87893", artistDetails?.playlists?.get(0)?.id)
        assertEquals(
            "https://yt3.googleusercontent.com/hzgUjSLVPf87Pp72xZQMhCe7e1m-S2bxtQBrLXOj7cxriyRkRdJP2KIyWJvkiN_me6rhaeUBGqE=s192",
            artistDetails?.playlists?.get(0)?.images?.firstOrNull()?.url
        )

    }
}

