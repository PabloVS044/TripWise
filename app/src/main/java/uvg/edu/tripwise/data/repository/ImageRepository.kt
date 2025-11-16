package uvg.edu.tripwise.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.UploadResponse
import java.io.File
import java.io.FileOutputStream

class ImageRepository(
    private val api: uvg.edu.tripwise.network.UserApiService = RetrofitInstance.api
) {
    private val MAX_BYTES = 5L * 1024 * 1024 // 5 MB

    suspend fun uploadImageFromUri(resolver: ContentResolver, uri: Uri): String {
        // 1) MIME real
        val mime = (resolver.getType(uri) ?: "image/jpeg").let { m ->
            when {
                m.startsWith("image/") -> m
                else -> "image/jpeg"
            }
        }

        // 2) Extensión y filename
        val ext = when (mime) {
            "image/png"  -> ".png"
            "image/webp" -> ".webp"
            else         -> ".jpg"
        }
        val displayName = queryDisplayName(resolver, uri)
        val safeFileName = displayName?.takeIf { it.contains('.') } ?: "photo_${System.currentTimeMillis()}$ext"

        // 3) Determinar tamaño para decidir si comprimimos
        val length = resolver.openAssetFileDescriptor(uri, "r")?.length ?: -1L
        val tmp = File.createTempFile("tw_upload_", ext)

        if (length in 1..Long.MAX_VALUE && length <= MAX_BYTES) {
            // copiar tal cual
            resolver.openInputStream(uri)!!.use { input ->
                FileOutputStream(tmp).use { out -> input.copyTo(out) }
            }
        } else {
            // Comprimir a JPEG calidad 85 si es grande o tamaño desconocido
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error("No se pudo leer la imagen")
            FileOutputStream(tmp).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        }

        // 4) Part con MIME y filename correctos
        val body = tmp.asRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData(
            /* name = */ "imagen",      // <- el backend espera este campo
            /* filename = */ safeFileName,
            /* body = */ body
        )

        try {
            val resp: UploadResponse = api.uploadImage(part)
            return resp.url
        } catch (e: HttpException) {
            val serverMsg = e.response()?.errorBody()?.string()
            throw RuntimeException(serverMsg ?: "Error ${e.code()} al subir imagen")
        } finally {
            tmp.delete()
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        } finally {
            cursor?.close()
        }
    }
}
