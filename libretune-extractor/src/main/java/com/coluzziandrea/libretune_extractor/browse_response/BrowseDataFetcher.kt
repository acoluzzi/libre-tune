package com.coluzziandrea.libretune_extractor.browse_response

import com.coluzziandrea.libretune_extractor.utils.decodeJsonLikeString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.logging.Logger


class BrowseDataFetcher(
    private val client: OkHttpClient
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val BASE_URL = "https://music.youtube.com"


    fun fetchBrowseData(url: String): BrowseData? {
        Logger.getLogger("BrowseDataFetcher").info {
            "Fetching browse data from URL: $url"
        }
        val fullUrl = if (url.startsWith("http")) url else "$BASE_URL$url"
        val request = Request.Builder().header(
            "User-Agent",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"
        )
            .header("Accept-Language", "en-US,en;q=0.9")
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
            ).url(fullUrl).build()
        val response = client.newCall(request).execute()

        Logger.getLogger("BrowseDataFetcher").info {
            "Received response for: $url"
        }
        if (!response.isSuccessful) {
            Logger.getLogger("BrowseDataFetcher").warning {
                "No response from URL: $url"
            }
            return null
        }

        val htmlBody = response.body.string()

        Logger.getLogger("BrowseDataFetcher")
            .info("Extracting JSON data from the HTML body")
        val jsonStart = htmlBody.lastIndexOf("data: '") + "data: '".length
        val jsonEnd = htmlBody.lastIndexOf("'}")
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            Logger.getLogger("ArtistScraper")
                .warning("Could not find JSON data in the browse data")
            return null
        }
        val jsonDataString = htmlBody.substring(jsonStart, jsonEnd)

        Logger.getLogger("BrowseDataFetcher")
            .info("Unescaping hex characters in JSON data")
        val cleanText = decodeJsonLikeString(jsonDataString)


        Logger.getLogger("BrowseDataFetcher")
            .info("Parsing JSON data into BrowseData object")
        val result = jsonParser.decodeFromString<BrowseData>(cleanText)

        Logger.getLogger("BrowseDataFetcher")
            .info("Successfully parsed BrowseData")
        return result
    }
}
