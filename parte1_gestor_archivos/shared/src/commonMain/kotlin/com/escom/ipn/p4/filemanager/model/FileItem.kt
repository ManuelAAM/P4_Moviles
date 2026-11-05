package com.escom.ipn.p4.filemanager.model

import kotlinx.serialization.Serializable

enum class FileCategory {
    FOLDER,
    TEXT,
    JSON,
    XML,
    IMAGE,
    AUDIO,
    DOCUMENT,
    OTHER
}

@Serializable
data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val formattedSize: String = "0 B",
    val lastModifiedEpochMs: Long = 0L,
    val formattedDate: String = "No disponible",
    val mimeType: String = "application/octet-stream",
    val isFavorite: Boolean = false,
    val extension: String = "",
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val category: FileCategory = FileCategory.OTHER
) {
    companion object {
        fun determineCategory(name: String, isDirectory: Boolean): FileCategory {
            if (isDirectory) return FileCategory.FOLDER
            val ext = name.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "txt", "md", "csv", "log" -> FileCategory.TEXT
                "json" -> FileCategory.JSON
                "xml", "html", "xhtml" -> FileCategory.XML
                "jpg", "jpeg", "png", "webp", "bmp", "gif" -> FileCategory.IMAGE
                "mp3", "wav", "m4a", "ogg", "aac" -> FileCategory.AUDIO
                "pdf", "doc", "docx" -> FileCategory.DOCUMENT
                else -> FileCategory.OTHER
            }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var size = bytes.toDouble()
            var unitIndex = 0
            while (size >= 1024 && unitIndex < units.size - 1) {
                size /= 1024
                unitIndex++
            }
            val sizeStr = if (size % 1.0 == 0.0) {
                size.toLong().toString()
            } else {
                val factor = 10.0
                val rounded = kotlin.math.round(size * factor) / factor
                rounded.toString()
            }
            return "$sizeStr ${units[unitIndex]}"
        }
    }
}

enum class AppColorTheme {
    GUINDA_IPN,
    AZUL_ESCOM
}
