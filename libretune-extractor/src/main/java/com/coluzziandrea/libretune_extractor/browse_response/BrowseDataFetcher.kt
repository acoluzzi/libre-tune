package com.coluzziandrea.libretune_extractor.browse_response

import com.coluzziandrea.libretune_extractor.utils.unescapeHex
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
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

        if (!response.isSuccessful) {
            return null
        }

        val htmlBody = response.body.string()

        // --- Part 2: Parse the HTML with Jsoup ---
        val document = Jsoup.parse(htmlBody)

        val scriptElements =
            document.select("script:containsData(try {const initialData = )")
        if (scriptElements.isEmpty()) {
            Logger.getLogger("ArtistScraper")
                .warning("No script elements found containing initialData")
            return null
        }
        val scriptContent = scriptElements.firstOrNull()?.data()
        if (scriptContent == null) {
            Logger.getLogger("ArtistScraper")
                .warning("Script content is null")
            return null
        }

        val doubleEscapesRemoved = scriptContent.replace("\\\"", "\"")
            // Replace \\" with "
            .replace("\\\\", "\\")   // Replace \\ with \
        val cleanText = unescapeHex(doubleEscapesRemoved)

        val browseDataStart =
            cleanText.indexOf("initialData.push({path: '\\/browse") + "initialData.push(".length
        val browseDataEnd =
            cleanText.indexOf("});ytcfg.set({'YTMUSIC_INITIAL_DATA", browseDataStart) + 1
        if (browseDataStart == -1 || browseDataEnd == -1 || browseDataEnd <= browseDataStart) {
            Logger.getLogger("ArtistScraper")
                .warning("Could not find browse data in the script content")
            return null
        }
        val browseData = cleanText.substring(browseDataStart, browseDataEnd)

        val jsonStart = browseData.indexOf("data: '") + "data: '".length
        val jsonEnd = browseData.lastIndexOf("'}")
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            Logger.getLogger("ArtistScraper")
                .warning("Could not find JSON data in the browse data")
            return null
        }
        val jsonData = browseData.substring(jsonStart, jsonEnd)


        return jsonParser.decodeFromString<BrowseData>(jsonData)
    }
}
