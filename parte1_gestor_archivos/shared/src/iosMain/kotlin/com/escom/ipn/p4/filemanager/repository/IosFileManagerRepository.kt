package com.escom.ipn.p4.filemanager.repository

import com.escom.ipn.p4.filemanager.model.FileCategory
import com.escom.ipn.p4.filemanager.model.FileItem

class IosFileManagerRepository : IFileManagerRepository {

    private val favorites = mutableSetOf<String>()
    private val recents = mutableListOf<String>()

    override suspend fun getRootDirectories(): List<FileItem> {
        return listOf(
            FileItem(
                path = "/Documents",
                name = "Espacio Privado Sandbox (iOS Documents)",
                isDirectory = true,
                category = FileCategory.FOLDER
            )
        )
    }

    override suspend fun listFiles(directoryPath: String): List<FileItem> {
        return emptyList()
    }

    override suspend fun readFileText(filePath: String): Result<String> {
        return Result.success("Contenido simulado en iOS Sandbox para $filePath")
    }

    override suspend fun readFileBytes(filePath: String): Result<ByteArray> {
        return Result.success("Contenido simulado en iOS Sandbox para $filePath".encodeToByteArray())
    }

    override suspend fun writeFileText(filePath: String, content: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun createDirectory(parentPath: String, name: String): Result<FileItem> {
        val path = "$parentPath/$name"
        return Result.success(
            FileItem(
                path = path,
                name = name,
                isDirectory = true,
                category = FileCategory.FOLDER
            )
        )
    }

    override suspend fun deleteItem(path: String): Result<Unit> {
        favorites.remove(path)
        recents.remove(path)
        return Result.success(Unit)
    }

    override suspend fun renameItem(path: String, newName: String): Result<FileItem> {
        val parent = path.substringBeforeLast('/', "")
        val newPath = if (parent.isEmpty()) newName else "$parent/$newName"
        return Result.success(
            FileItem(
                path = newPath,
                name = newName,
                isDirectory = false,
                category = FileItem.determineCategory(newName, false)
            )
        )
    }

    override suspend fun copyItem(sourcePath: String, destDirPath: String): Result<FileItem> {
        val name = sourcePath.substringAfterLast('/')
        val targetPath = "$destDirPath/$name"
        return Result.success(
            FileItem(
                path = targetPath,
                name = name,
                isDirectory = false,
                category = FileItem.determineCategory(name, false)
            )
        )
    }

    override suspend fun moveItem(sourcePath: String, destDirPath: String): Result<FileItem> {
        return copyItem(sourcePath, destDirPath)
    }

    override fun getFavorites(): Set<String> = favorites.toSet()

    override fun toggleFavorite(path: String): Boolean {
        return if (favorites.contains(path)) {
            favorites.remove(path)
            false
        } else {
            favorites.add(path)
            true
        }
    }

    override fun getRecents(): List<String> = recents.toList()

    override fun addRecent(path: String) {
        recents.remove(path)
        recents.add(0, path)
    }

    override fun isFavorite(path: String): Boolean = favorites.contains(path)
}
