package com.escom.ipn.p4.filemanager.model

import kotlinx.serialization.Serializable

@Serializable
data class HttpProxyRequest(
    val requestId: String,
    val targetUrl: String,
    val httpMethod: String = "GET",
    val requestedAtMillis: Long
)

@Serializable
data class HttpProxyResponse(
    val requestId: String,
    val targetUrl: String,
    val statusCode: Int,
    val body: String,
    val contentType: String = "application/json",
    val durationMs: Long = 0L,
    val fromCache: Boolean = false,
    val cachedAtMillis: Long? = null,
    val errorMessage: String? = null
)

@Serializable
data class WebCacheRecord(
    val url: String,
    val response: HttpProxyResponse,
    val savedAtMillis: Long,
    val humanReadableDate: String
)

enum class BluetoothConnectionRole {
    STANDALONE,
    SERVER_GATEWAY, // Dispositivo A con Internet
    CLIENT_VIEWER   // Dispositivo B sin Internet
}
