package com.escom.ipn.p4.filemanager.service

expect object Sha256Util {
    fun hash(bytes: ByteArray): String
    fun hash(text: String): String
}
