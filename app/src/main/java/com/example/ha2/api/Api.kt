package com.example.ha2.api

class Api {
    fun getRequestUrl(uri: String) = Settings.server.plus(uri)
}