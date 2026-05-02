package com.colux.libretune.shared

private class DesktopPlatform : Platform {
    override val name: String =
        "Desktop (${System.getProperty("os.name")} ${System.getProperty("os.version")})"
}

actual fun currentPlatform(): Platform = DesktopPlatform()
