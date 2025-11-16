package uvg.edu.tripwise.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun createMultipartBodyFromUri(uri: Uri, context: Context, partName: String): MultipartBody.Part {
    val contentResolver = context.contentResolver

    // Obtener el tipo MIME (p.ej. "image/jpeg")
    val mimeType = contentResolver.getType(uri)

    // Obtener el nombre del archivo
    var fileName = "unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }

    // Leer los bytes del archivo
    val inputStream = contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("No se puede abrir el archivo desde la Uri")

    val fileBytes = inputStream.readBytes()
    inputStream.close()

    // Crear el RequestBody
    val requestBody = fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())

    // Crear el MultipartBody.Part
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}