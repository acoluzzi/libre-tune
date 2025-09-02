package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val clientName: String,
    val clientVersion: String
)

@Serializable
data class Context(
    val client: Client
)