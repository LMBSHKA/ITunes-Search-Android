package com.example.itunes_search.data.local

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.itunes_search.domain.repository.ProfilePhotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ProfilePhotoFiles(private val context: Context) : ProfilePhotoStorage {
    private val resolver = context.contentResolver
    private val authority = "${context.packageName}.profile-files"
    private val drafts = File(context.cacheDir, "profile_photos")
    private val avatars = File(context.filesDir, "profile_avatars")

    override suspend fun copyGalleryPhoto(sourceUri: String): String = withContext(Dispatchers.IO) {
        copyPhoto(sourceUri, drafts)
    }

    override fun createCameraOutput(): String = newPhotoFile(drafts, "jpg").let(::contentUri)

    override fun hasCameraPhoto(uri: String): Boolean = resolver.openFileDescriptor(Uri.parse(uri), "r")
        ?.use { it.statSize > 0 } == true

    override fun discardDraft(uri: String) = deletePhoto(uri, "draft_photos")

    fun saveAvatar(uri: String): String = copyPhoto(uri, avatars)

    fun deleteAvatar(uri: String) = deletePhoto(uri, "avatars")

    private fun copyPhoto(sourceUri: String, directory: File): String {
        val uri = Uri.parse(sourceUri)
        val extension = resolver.getType(uri)?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
        val file = newPhotoFile(directory, extension ?: "img")
        try {
            val size = resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: 0L
            if (size == 0L) throw IOException("Empty photo")
            return contentUri(file)
        } catch (exception: Exception) {
            file.delete()
            throw exception
        }
    }

    private fun newPhotoFile(directory: File, extension: String): File {
        if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
            throw IOException("Cannot create profile photo directory")
        }
        return File.createTempFile("photo-", ".$extension", directory)
    }

    private fun contentUri(file: File): String = FileProvider.getUriForFile(context, authority, file).toString()

    private fun deletePhoto(value: String, root: String) {
        val uri = Uri.parse(value)
        if (uri.authority == authority && uri.pathSegments.firstOrNull() == root) {
            resolver.delete(uri, null, null)
        }
    }
}
