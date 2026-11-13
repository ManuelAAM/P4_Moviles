package com.escom.ipn.p4.filemanager.repository

import android.content.Context
import android.content.SharedPreferences
import android.webkit.MimeTypeMap
import com.escom.ipn.p4.filemanager.model.FileCategory
import com.escom.ipn.p4.filemanager.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidFileManagerRepository(
    private val context: Context
) : IFileManagerRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kmp_file_manager_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    init {
        // Inicializar datos demo en el almacenamiento privado de la app para pruebas inmediatas offline
        seedDemoFilesIfEmpty()
    }

    private fun seedDemoFilesIfEmpty() {
        val root = context.filesDir
        val seededMarker = File(root, ".seeded_v1")
        if (!seededMarker.exists()) {
            try {
                // Carpeta Documentos
                val docsDir = File(root, "Documentos")
                if (!docsDir.exists()) docsDir.mkdirs()

                // Archivo TXT
                val txtFile = File(docsDir, "bienvenida_ipn.txt")
                if (!txtFile.exists()) {
                    txtFile.writeText(
                        "INSTITUTO POLITÉCNICO NACIONAL\n" +
                        "ESCUELA SUPERIOR DE CÓMPUTO\n\n" +
                        "Práctica 4: Gestión de archivos multiplataforma (Android e iOS)\n" +
                        "Tecnología: Kotlin Multiplatform (KMP) + Jetpack Compose\n\n" +
                        "Características del gestor:\n" +
                        "- Exploración segura de almacenamiento privado y SAF\n" +
                        "- Resaltado de sintaxis para JSON y XML\n" +
                        "- Visor de imágenes con Zoom y Rotación\n" +
                        "- Historial de recientes y favoritos persistentes\n" +
                        "- Temas Guinda IPN y Azul ESCOM\n"
                    )
                }

                // Archivo Markdown
                val mdFile = File(docsDir, "especificaciones.md")
                if (!mdFile.exists()) {
                    mdFile.writeText(
                        "# Especificaciones de la Práctica 4\n\n" +
                        "## Módulos Desarrollados\n" +
                        "1. **Gestor de Archivos Multiplataforma** (`parte1_gestor_archivos`)\n" +
                        "2. **Cyber Breach (Net Strike)** (`parte2_cyber_breach`)\n\n" +
                        "## Cumplimiento de Calidad\n" +
                        "- Arquitectura Clean en KMP\n" +
                        "- 100% Offline y seguro bajo Scoped Storage\n"
                    )
                }

                // Carpeta Datos
                val dataDir = File(root, "Datos")
                if (!dataDir.exists()) dataDir.mkdirs()

                // Archivo JSON
                val jsonFile = File(dataDir, "alumnos_escom.json")
                if (!jsonFile.exists()) {
                    jsonFile.writeText(
                        "{\n" +
                        "  \"institucion\": \"Instituto Politécnico Nacional\",\n" +
                        "  \"escuela\": \"Escuela Superior de Cómputo\",\n" +
                        "  \"asignatura\": \"Desarrollo de Aplicaciones Móviles Nativas\",\n" +
                        "  \"grupo\": \"7CV4\",\n" +
                        "  \"periodo\": \"2027-1\",\n" +
                        "  \"practica\": 4,\n" +
                        "  \"integrantes\": [\n" +
                        "    {\n" +
                        "      \"nombre\": \"Hernández Alvirde María Guadalupe\",\n" +
                        "      \"boleta\": \"2022630105\",\n" +
                        "      \"rol\": \"Desarrollador KMP\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"nombre\": \"Aragón Martínez Manuel Alejandro\",\n" +
                        "      \"boleta\": \"2023630411\",\n" +
                        "      \"rol\": \"Desarrollador KMP\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"estado_proyecto\": {\n" +
                        "    \"completado\": true,\n" +
                        "    \"version\": \"1.0.0\",\n" +
                        "    \"offline_ready\": true\n" +
                        "  }\n" +
                        "}"
                    )
                }

                // Archivo XML
                val xmlFile = File(dataDir, "configuracion_red.xml")
                if (!xmlFile.exists()) {
                    xmlFile.writeText(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<CyberNetworkConfig environment=\"Production\">\n" +
                        "    <SecurityPolicy level=\"High\" firewallActive=\"true\">\n" +
                        "        <Node name=\"Gateway\" ip=\"192.168.1.1\" port=\"443\" status=\"ONLINE\" />\n" +
                        "        <Node name=\"Database\" ip=\"10.0.0.5\" port=\"5432\" status=\"SECURE\" />\n" +
                        "        <Node name=\"WebServer\" ip=\"172.16.0.10\" port=\"80\" status=\"ACTIVE\" />\n" +
                        "    </SecurityPolicy>\n" +
                        "    <AuditLog enabled=\"true\" maxRetentionDays=\"30\">\n" +
                        "        <Event type=\"SYNC\" timestamp=\"2026-11-04T00:00:00Z\" />\n" +
                        "    </AuditLog>\n" +
                        "</CyberNetworkConfig>"
                    )
                }

                seededMarker.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Garantizar que la imagen de ejemplo para pruebas de sensores esté disponible en Documentos y en Raíz
        try {
            val docsDir = File(root, "Documentos")
            if (!docsDir.exists()) docsDir.mkdirs()
            val sampleImgDocs = File(docsDir, "imagen_ejemplo_petergriffin.jpg")
            if (!sampleImgDocs.exists() || sampleImgDocs.length() == 0L) {
                context.assets.open("imagen_ejemplo_petergriffin.jpg").use { input ->
                    sampleImgDocs.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val sampleImgRoot = File(root, "imagen_ejemplo_petergriffin.jpg")
            if (!sampleImgRoot.exists() || sampleImgRoot.length() == 0L) {
                context.assets.open("imagen_ejemplo_petergriffin.jpg").use { input ->
                    sampleImgRoot.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (ignored: Exception) {}
    }

    override suspend fun getRootDirectories(): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()
        // 1. Almacenamiento privado de la aplicación
        val internalDir = context.filesDir
        list.add(fileToFileItem(internalDir, "Espacio Privado de la Aplicación"))

        // 2. Almacenamiento externo propio de la app (si existe)
        val extDir = context.getExternalFilesDir(null)
        if (extDir != null && extDir.exists()) {
            list.add(fileToFileItem(extDir, "Almacenamiento Externo de la App"))
        }

        // 3. Directorio de caché
        val cacheDir = context.cacheDir
        if (cacheDir != null && cacheDir.exists()) {
            list.add(fileToFileItem(cacheDir, "Caché de la Aplicación"))
        }

        list
    }

    override suspend fun listFiles(directoryPath: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val files = dir.listFiles() ?: return@withContext emptyList()
        val favorites = getFavorites()

        files.map { file ->
            val item = fileToFileItem(file)
            item.copy(isFavorite = favorites.contains(item.path))
        }.sortedWith(
            compareByDescending<FileItem> { it.isDirectory }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        )
    }

    override suspend fun readFileText(filePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext Result.failure(Exception("El archivo no existe."))
            if (file.isDirectory) return@withContext Result.failure(Exception("La ruta indicada es un directorio."))
            val text = file.readText(Charsets.UTF_8)
            addRecent(filePath)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readFileBytes(filePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val file = if (File(filePath).isAbsolute) File(filePath) else File(context.filesDir, filePath)
            if (!file.exists()) return@withContext Result.failure(Exception("El archivo no existe."))
            if (file.isDirectory) return@withContext Result.failure(Exception("La ruta indicada es un directorio."))
            val bytes = file.readBytes()
            addRecent(file.absolutePath)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun writeFileText(filePath: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content, Charsets.UTF_8)
            addRecent(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDirectory(parentPath: String, name: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val dir = File(parentPath, name.trim())
            if (dir.exists()) return@withContext Result.failure(Exception("Ya existe una carpeta o archivo con ese nombre."))
            val created = dir.mkdirs()
            if (created) {
                Result.success(fileToFileItem(dir))
            } else {
                Result.failure(Exception("No se pudo crear el directorio. Verifique permisos."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext Result.failure(Exception("El elemento no existe."))
            val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
            if (deleted) {
                // Quitar de favoritos si estaba
                val favs = getFavorites().toMutableSet()
                if (favs.remove(path)) {
                    saveFavorites(favs)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo eliminar el elemento."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameItem(path: String, newName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val source = File(path)
            if (!source.exists()) return@withContext Result.failure(Exception("El archivo de origen no existe."))
            val target = File(source.parentFile, newName.trim())
            if (target.exists()) return@withContext Result.failure(Exception("Ya existe un elemento con el nuevo nombre."))
            val success = source.renameTo(target)
            if (success) {
                // Actualizar favoritos si estaba
                val favs = getFavorites().toMutableSet()
                if (favs.remove(path)) {
                    favs.add(target.absolutePath)
                    saveFavorites(favs)
                }
                Result.success(fileToFileItem(target))
            } else {
                Result.failure(Exception("Fallo al renombrar el elemento."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun copyItem(sourcePath: String, destDirPath: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            if (!source.exists()) return@withContext Result.failure(Exception("El origen no existe."))
            val destDir = File(destDirPath)
            if (!destDir.exists() || !destDir.isDirectory) return@withContext Result.failure(Exception("El directorio destino no es válido."))

            val targetFile = File(destDir, source.name)
            val finalTarget = if (targetFile.exists()) {
                val base = source.nameWithoutExtension
                val ext = if (source.extension.isNotEmpty()) ".${source.extension}" else ""
                File(destDir, "${base}_copia$ext")
            } else {
                targetFile
            }

            if (source.isDirectory) {
                source.copyRecursively(finalTarget, overwrite = true)
            } else {
                source.copyTo(finalTarget, overwrite = true)
            }
            Result.success(fileToFileItem(finalTarget))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveItem(sourcePath: String, destDirPath: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val copyResult = copyItem(sourcePath, destDirPath)
            if (copyResult.isSuccess) {
                deleteItem(sourcePath)
                copyResult
            } else {
                copyResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorite_files", emptySet()) ?: emptySet()
    }

    override fun toggleFavorite(path: String): Boolean {
        val current = getFavorites().toMutableSet()
        val newState = if (current.contains(path)) {
            current.remove(path)
            false
        } else {
            current.add(path)
            true
        }
        saveFavorites(current)
        return newState
    }

    private fun saveFavorites(favs: Set<String>) {
        prefs.edit().putStringSet("favorite_files", favs).apply()
    }

    override fun isFavorite(path: String): Boolean {
        return getFavorites().contains(path)
    }

    override fun getRecents(): List<String> {
        val raw = prefs.getString("recent_files_list", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(";").filter { it.isNotBlank() && File(it).exists() }
    }

    override fun addRecent(path: String) {
        val current = getRecents().toMutableList()
        current.remove(path)
        current.add(0, path)
        if (current.size > 20) {
            current.removeAt(current.size - 1)
        }
        prefs.edit().putString("recent_files_list", current.joinToString(";")).apply()
    }

    private fun fileToFileItem(file: File, customDisplayName: String? = null): FileItem {
        val isDir = file.isDirectory
        val ext = file.extension.lowercase(Locale.ROOT)
        val mime = if (isDir) {
            "resource/folder"
        } else {
            val m = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            m ?: "application/octet-stream"
        }

        val size = if (isDir) 0L else file.length()
        val lastMod = file.lastModified()
        val formattedDate = if (lastMod > 0) dateFormat.format(Date(lastMod)) else "No disponible"

        return FileItem(
            path = file.absolutePath,
            name = customDisplayName ?: file.name,
            isDirectory = isDir,
            sizeBytes = size,
            formattedSize = if (isDir) "Carpeta" else FileItem.formatBytes(size),
            lastModifiedEpochMs = lastMod,
            formattedDate = formattedDate,
            mimeType = mime,
            extension = ext,
            canRead = file.canRead(),
            canWrite = file.canWrite(),
            category = FileItem.determineCategory(file.name, isDir)
        )
    }
}
