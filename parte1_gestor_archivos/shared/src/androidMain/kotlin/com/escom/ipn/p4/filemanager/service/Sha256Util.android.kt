package com.escom.ipn.p4.filemanager.service

import java.security.MessageDigest

actual object Sha256Util {
    actual fun hash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    actual fun hash(text: String): String {
        return hash(text.toByteArray(Charsets.UTF_8))
    }
}
