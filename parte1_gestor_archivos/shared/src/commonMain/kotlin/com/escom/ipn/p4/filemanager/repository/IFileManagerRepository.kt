package com.escom.ipn.p4.filemanager.repository

import com.escom.ipn.p4.filemanager.model.FileItem

interface IFileManagerRepository {
    suspend fun getRootDirectories(): List<FileItem>
    suspend fun listFiles(directoryPath: String): List<FileItem>
    suspend fun readFileText(filePath: String): Result<String>
    suspend fun readFileBytes(filePath: String): Result<ByteArray>
    suspend fun writeFileText(filePath: String, content: String): Result<Unit>
    suspend fun createDirectory(parentPath: String, name: String): Result<FileItem>
    suspend fun deleteItem(path: String): Result<Unit>
    suspend fun renameItem(path: String, newName: String): Result<FileItem>
    suspend fun copyItem(sourcePath: String, destDirPath: String): Result<FileItem>
    suspend fun moveItem(sourcePath: String, destDirPath: String): Result<FileItem>
    
    fun getFavorites(): Set<String>
    fun toggleFavorite(path: String): Boolean
    fun getRecents(): List<String>
    fun addRecent(path: String)
    fun isFavorite(path: String): Boolean
}
