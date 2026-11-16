package com.example.itunes_search.data.remote

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import com.example.itunes_search.domain.model.ResumeDocument
import com.example.itunes_search.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.UUID

class NetworkResumeRepository(
    private val context: Context,
    private val client: OkHttpClient
) : ResumeRepository {
    override suspend fun download(url: String): ResumeDocument = withContext(Dispatchers.IO) {
        val address = url.toHttpUrlOrNull()?.takeIf { it.isHttps }
            ?: throw IOException("Invalid HTTPS URL")
        val request = Request.Builder().url(address).build()
        client.newCall(request).execute().use { response ->
            val body = response.body ?: throw IOException("Empty response")
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val responseType = body.contentType()?.let { "${it.type}/${it.subtype}" }
            if (responseType == "text/html" || responseType == "application/xhtml+xml") {
                throw IOException("The link points to a web page")
            }
            val knownType = responseType?.takeUnless { it == "application/octet-stream" }
            val guessedName = URLUtil.guessFileName(
                response.request.url.toString(),
                response.header("Content-Disposition"),
                knownType
            )
            val name = File(guessedName).name.trimStart('.').takeLast(120).ifBlank { "resume" }
            val directory = File(context.cacheDir, "profile_resumes/${UUID.randomUUID()}")
            if (!directory.mkdirs()) throw IOException("Cannot create resume directory")

            val file = File(directory, name)
            try {
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            ensureActive()
                            val count = input.read(buffer)
                            if (count == -1) break
                            output.write(buffer, 0, count)
                        }
                    }
                }
                if (file.length() == 0L) throw IOException("Empty document")
                val mimeType = knownType
                    ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
                    ?: "application/octet-stream"
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.profile-files", file)
                ResumeDocument(uri.toString(), mimeType)
            } catch (exception: Exception) {
                directory.deleteRecursively()
                throw exception
            }
        }
    }
}
