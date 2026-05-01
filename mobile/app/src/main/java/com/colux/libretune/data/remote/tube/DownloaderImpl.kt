package com.colux.libretune.data.remote.tube

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.TimeUnit


class DownloaderImpl private constructor(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : Downloader() {

    override fun execute(request: Request): Response {
        val okRequest = okhttp3.Request.Builder()
            .url(request.url())
            .method(request.httpMethod(), request.dataToSend()?.toRequestBody())
            .build()

        val response = client.newCall(okRequest).execute()

        val responseBodyString = response.body?.string()

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBodyString,
            response.request.url.toString()
        )
    }

    companion object {
        fun init(builder: OkHttpClient.Builder?): DownloaderImpl {
            return DownloaderImpl()
        }
    }
}