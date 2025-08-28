package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.model.Album
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.response.BrowseData
import com.coluzziandrea.libretune_extractor.response.tab.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint.NavigationEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistScraper @Inject constructor(
    private val client: OkHttpClient
) {


    suspend fun scrapeArtistPage(channelId: String): ArtistDetails? {
        return withContext(Dispatchers.IO) {
            try {
                // --- Part 1: Fetch the Page HTML ---
                val url = "https://music.youtube.com/channel/$channelId"
                val request = Request.Builder().header(
                    "User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"
                )
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
                    ).url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext null
                }

                val htmlBody = response.body.string()

                // --- Part 2: Parse the HTML with Jsoup ---
                val document = Jsoup.parse(htmlBody)

                // --- Part 3: Search for Content ---

                // Simple metadata is often in <meta> tags, which is more stable
                val artistName = document.select("meta[property=og:title]").attr("content")
                val description = document.select("meta[property=og:description]").attr("content")
                val bannerUrl = document.select("meta[property=og:image]").attr("content")

                val (topSongs, albums) = extractTopSongsAndAlbums(document)


                // For this skeleton, we will return what we can easily get.
                ArtistDetails(
                    name = artistName,
                    description = description,
                    bannerUrl = bannerUrl,
                    topSongs = topSongs, // This would come from the complex JSON parsing
                    albums = albums,
                    similarArtists = emptyList()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    private fun extractTopSongsAndAlbums(document: Document): Pair<List<Song>, List<Album>> {
        val topSongs = mutableListOf<Song>()
        val albums = mutableListOf<Album>()
        val scriptElements =
            document.select("script:containsData(try {const initialData = )")
        if (scriptElements.isEmpty()) {
            Logger.getLogger("ArtistScraper")
                .warning("No script elements found containing initialData")
            return Pair(topSongs, albums)
        }
        val scriptContent = scriptElements.firstOrNull()?.data()
        if (scriptContent == null) {
            Logger.getLogger("ArtistScraper")
                .warning("Script content is null")
            return Pair(topSongs, albums)
        }

        val doubleEscapesRemoved = scriptContent.replace("\\\"", "\"")
            // Replace \\" with "
            .replace("\\\\", "\\")   // Replace \\ with \
        val cleanText = unescapeHex(doubleEscapesRemoved)

        val browseDataStart =
            cleanText.indexOf("initialData.push({path: '\\/browse") + "initialData.push(".length
        val browseDataEnd = cleanText.indexOf("});ytcfg.set", browseDataStart) + 1
        if (browseDataStart == -1 || browseDataEnd == -1 || browseDataEnd <= browseDataStart) {
            Logger.getLogger("ArtistScraper")
                .warning("Could not find browse data in the script content")
            return Pair(topSongs, albums)
        }
        val browseData = cleanText.substring(browseDataStart, browseDataEnd)

        val jsonStart = browseData.indexOf("data: '") + "data: '".length
        val jsonEnd = browseData.lastIndexOf("'}")
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            Logger.getLogger("ArtistScraper")
                .warning("Could not find JSON data in the browse data")
            return Pair(topSongs, albums)
        }
        val jsonData = browseData.substring(jsonStart, jsonEnd)


        val browseDataObject = jsonParser.decodeFromString<BrowseData>(jsonData)

        browseDataObject.contents.singleColumnBrowseResultsRenderer.tabs.forEach { tab ->
            tab.tabRenderer.content.sectionListRenderer.contents.forEach { content ->
                when (content) {
                    is SectionContent.MusicShelfContent -> {
                        if (content.musicShelfRenderer.title.runs.find {
                                it.text == "Top songs"
                            } != null) {

                            content.musicShelfRenderer.contents.forEach { shelfItem ->
                                if (shelfItem is SectionContent.MusicResponsiveListItemContent) {
                                    val titleFlex =
                                        shelfItem.musicResponsiveListItemRenderer.flexColumns.find { flex ->
                                            flex.musicResponsiveListItemFlexColumnRenderer.text.runs.find { run ->
                                                run.navigationEndpoint != null && run.navigationEndpoint is NavigationEndpoint.WatchNavigationEndpoint
                                                        && run.navigationEndpoint.watchEndpoint.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType == "MUSIC_VIDEO_TYPE_ATV"
                                            } != null
                                        }

                                    topSongs.add(
                                        Song(
                                            id = shelfItem.musicResponsiveListItemRenderer.overlay.musicItemThumbnailOverlayRenderer.content.musicPlayButtonRenderer.playNavigationEndpoint.watchEndpoint.videoId,
                                            playlistId = shelfItem.musicResponsiveListItemRenderer.overlay.musicItemThumbnailOverlayRenderer.content.musicPlayButtonRenderer.playNavigationEndpoint.watchEndpoint.playlistId,
                                            imageUrl = shelfItem.musicResponsiveListItemRenderer.thumbnail.musicThumbnailRenderer.thumbnail.thumbnails.firstOrNull()?.url,
                                            title = titleFlex?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.get(
                                                0
                                            )?.text ?: ""
                                        )
                                    )
                                }

                            }

                        }
                    }

                    is SectionContent.MusicCarouselContent -> {
                        if (content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.runs.find {
                                it.text == "Albums"
                            } != null) {

                            content.musicCarouselShelfRenderer.contents.forEach { carouselItem ->

                                albums.add(
                                    Album(
                                        id = (carouselItem.musicTwoRowItemRenderer.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                                        name = carouselItem.musicTwoRowItemRenderer.title.runs[0].text,
                                        thumbnailUrl = carouselItem.musicTwoRowItemRenderer.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.firstOrNull()?.url
                                            ?: ""
                                    )
                                )

                            }

                        }
                    }

                    is SectionContent.MusicResponsiveListItemContent, is SectionContent.EmptyContent -> {
                        // DO NOTHING
                    }
                }
            }
        }


        return Pair(topSongs, albums)
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun unescapeHex(text: String): String {
        val result = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val char = text[i]
            if (char == '\\' && i + 3 < text.length && text[i + 1] == 'x') {
                val hex = text.substring(i + 2, i + 4)
                try {
                    result.append(hex.toInt(16).toChar())
                    i += 4 // Move index past the processed "\xHH" sequence
                    continue
                } catch (e: NumberFormatException) {
                    // Not a valid hex, append literally
                }
            }
            result.append(char)
            i++
        }
        return result.toString()
    }
}