package com.colux.libretune.shared

interface Platform {
    val name: String
}

expect fun currentPlatform(): Platform
