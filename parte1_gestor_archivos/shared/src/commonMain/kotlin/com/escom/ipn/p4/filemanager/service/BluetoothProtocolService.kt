package com.escom.ipn.p4.filemanager.service

import com.escom.ipn.p4.filemanager.model.FileChunkPacket
import com.escom.ipn.p4.filemanager.model.FileTransferMetadataPacket
import com.escom.ipn.p4.filemanager.model.HttpProxyRequest
import com.escom.ipn.p4.filemanager.model.HttpProxyResponse
import com.escom.ipn.p4.filemanager.model.TransferAckPacket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BluetoothProtocolService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    // Whitelist de dominios HTTPS autorizados para el proxy (mitigación SSRF)
    val ALLOWED_DOMAINS = listOf(
        "jsonplaceholder.typicode.com",
        "api.github.com",
        "worldtimeapi.org",
        "pokeapi.co",
        "ipn.mx",
        "escom.ipn.mx"
    )

    // Endpoints demo precargados para pruebas controladas
    val DEMO_PROXY_ENDPOINTS = listOf(
        "https://jsonplaceholder.typicode.com/posts/1",
        "https://jsonplaceholder.typicode.com/todos/1",
        "https://api.github.com/zen",
        "https://worldtimeapi.org/api/timezone/America/Mexico_City",
        "https://pokeapi.co/api/v2/pokemon/ditto"
    )

    fun sanitizeFileName(rawName: String): String {
        val clean = rawName
            .replace("\\", "/")
            .substringAfterLast("/")
            .filter { it.isLetterOrDigit() || it in "._- " }
            .trim()
        return if (clean.isBlank()) "archivo_recibido.bin" else clean
    }

    fun isUrlAllowed(url: String): Pair<Boolean, String?> {
        val trimmed = url.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) {
            return Pair(false, "Solo se permite el esquema seguro HTTPS (https://).")
        }

        val domainPart = trimmed.removePrefix("https://").substringBefore("/").substringBefore(":")

        // Bloqueo explícito de IPs privadas y localhost para mitigar SSRF
        val isPrivateIp = domainPart.equals("localhost", ignoreCase = true) ||
                domainPart.startsWith("127.") ||
                domainPart.startsWith("10.") ||
                domainPart.startsWith("192.168.") ||
                (domainPart.startsWith("172.") && domainPart.substringAfter("172.").substringBefore(".").toIntOrNull() in 16..31)

        if (isPrivateIp) {
            return Pair(false, "Destino privado o local bloqueado por política de seguridad SSRF.")
        }

        val isDomainWhitelisted = ALLOWED_DOMAINS.any { domainPart.equals(it, ignoreCase = true) || domainPart.endsWith(".$it", ignoreCase = true) }
        if (!isDomainWhitelisted) {
            return Pair(false, "El dominio '$domainPart' no está en la lista de recursos HTTPS autorizados.")
        }

        return Pair(true, null)
    }

    fun serializeMetadata(packet: FileTransferMetadataPacket): String =
        "META:" + json.encodeToString(packet)

    fun deserializeMetadata(payload: String): FileTransferMetadataPacket? = try {
        val raw = if (payload.startsWith("META:")) payload.removePrefix("META:") else payload
        json.decodeFromString<FileTransferMetadataPacket>(raw)
    } catch (_: Exception) {
        null
    }

    fun serializeChunk(packet: FileChunkPacket): String =
        "CHUNK:" + json.encodeToString(packet)

    fun deserializeChunk(payload: String): FileChunkPacket? = try {
        val raw = if (payload.startsWith("CHUNK:")) payload.removePrefix("CHUNK:") else payload
        json.decodeFromString<FileChunkPacket>(raw)
    } catch (_: Exception) {
        null
    }

    fun serializeAck(packet: TransferAckPacket): String =
        "ACK:" + json.encodeToString(packet)

    fun deserializeAck(payload: String): TransferAckPacket? = try {
        val raw = if (payload.startsWith("ACK:")) payload.removePrefix("ACK:") else payload
        json.decodeFromString<TransferAckPacket>(raw)
    } catch (_: Exception) {
        null
    }

    fun serializeProxyRequest(req: HttpProxyRequest): String =
        "HTTP_REQ:" + json.encodeToString(req)

    fun deserializeProxyRequest(payload: String): HttpProxyRequest? = try {
        val raw = if (payload.startsWith("HTTP_REQ:")) payload.removePrefix("HTTP_REQ:") else payload
        json.decodeFromString<HttpProxyRequest>(raw)
    } catch (_: Exception) {
        null
    }

    fun serializeProxyResponse(res: HttpProxyResponse): String =
        "HTTP_RES:" + json.encodeToString(res)

    fun deserializeProxyResponse(payload: String): HttpProxyResponse? = try {
        val raw = if (payload.startsWith("HTTP_RES:")) payload.removePrefix("HTTP_RES:") else payload
        json.decodeFromString<HttpProxyResponse>(raw)
    } catch (_: Exception) {
        null
    }
}
