package com.colux.libretune.data.remote.backend

interface BackendTokenStore {
    fun save(token: String)
    fun get(): String?
    fun clear()
    fun isAuthenticated(): Boolean
}
