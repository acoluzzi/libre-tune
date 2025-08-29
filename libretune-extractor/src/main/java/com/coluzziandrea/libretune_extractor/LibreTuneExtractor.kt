package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.browse_response.BrowseData
import com.coluzziandrea.libretune_extractor.browse_response.BrowseDataFetcher
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.parser.ArtistParser
import com.coluzziandrea.libretune_extractor.parser.PlaylistParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibreTuneExtractor @Inject constructor(
    private val client: OkHttpClient
) {

    private val logger = Logger.getLogger("LibreTuneExtractor")
    private val browseDataFetcher = BrowseDataFetcher(client)

    suspend fun playlist(playlistId: String): PlaylistDetails? {
        return fetchAndParseBrowseData("/browse/$playlistId", PlaylistParser.Companion::from)
    }


    suspend fun artist(channelId: String): ArtistDetails? {
        return fetchAndParseBrowseData("/channel/$channelId", ArtistParser.Companion::from)
    }


    private suspend fun <T> fetchAndParseBrowseData(url: String, parse: (BrowseData) -> T?): T? {
        return withContext(Dispatchers.IO) {
            try {
                logger
                    .info("Fetching browseData for url: $url")
                val browseDataObject = browseDataFetcher.fetchBrowseData(url)

                logger
                    .info("Fetched browseData for url: $url")
                if (browseDataObject == null) {
                    logger
                        .warning("Failed to fetch or parse browse data for url: $url")
                    return@withContext null
                }

                logger
                    .info("Parsing info from browseData for url: $url")
                val result = parse(browseDataObject)

                logger
                    .info("Parsed info from browseData for url: $url")
                return@withContext result
            } catch (e: Exception) {
                logger.severe("Error fetching or parsing browse data: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}