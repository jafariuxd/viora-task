package com.example.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object ImageUtil {
    const val BASE_BACKEND_URL = "http://45.195.250.77:3000"

    fun resolveAvatarUrl(uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null
        if (uriString.startsWith("http://") || uriString.startsWith("https://")) return uriString
        if (uriString.startsWith("/uploads/")) return "$BASE_BACKEND_URL$uriString"
        if (uriString.startsWith("uploads/")) return "$BASE_BACKEND_URL/$uriString"
        if (uriString.startsWith("data:image/") || uriString.startsWith("content://") || uriString.startsWith("file://")) return uriString
        if (uriString.startsWith("/") && !File(uriString).exists()) return "$BASE_BACKEND_URL$uriString"
        return uriString
    }

    fun copyUriToInternalStorage(context: Context, uriString: String?): String? {
        if (uriString == null) return null
        if (uriString.startsWith("file://") || uriString.startsWith("/data/")) return uriString
        
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString
            
            // Delete older avatars if they exist to save space
            try {
                val files = context.filesDir.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.name.startsWith("user_avatar_") && f.name.endsWith(".jpg")) {
                            f.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }

            val file = File(context.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            inputStream.close()
            outputStream.close()
            
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    fun toSmallBase64(context: Context, uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null
        if (uriString.startsWith("data:image/") || uriString.startsWith("http://") || uriString.startsWith("https://")) {
            return if (uriString.length <= 500) uriString else uriString.take(500)
        }
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString.take(500)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return uriString.take(500)

            var targetDim = 40
            var result: String? = null
            while (targetDim >= 12) {
                val width = originalBitmap.width
                val height = originalBitmap.height
                val ratio = Math.min(targetDim.toFloat() / width, targetDim.toFloat() / height)
                val newWidth = Math.max(1, Math.round(ratio * width))
                val newHeight = Math.max(1, Math.round(ratio * height))
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

                val outputStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 25, outputStream)
                val byteArray = outputStream.toByteArray()
                val b64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
                val dataUri = "data:image/jpeg;base64,$b64"

                if (dataUri.length <= 500) {
                    result = dataUri
                    break
                }
                targetDim -= 6
            }
            result ?: uriString.take(500)
        } catch (e: Exception) {
            e.printStackTrace()
            if (uriString.length <= 500) uriString else uriString.take(500)
        }
    }

    fun isLocalFileOrUriValid(uriString: String?): Boolean {
        if (uriString.isNullOrEmpty()) return false
        if (uriString.startsWith("data:image/") || uriString.startsWith("http://") || uriString.startsWith("https://")) {
            return true
        }
        if (uriString.startsWith("/uploads/") || uriString.startsWith("uploads/")) {
            return true
        }
        if (uriString.startsWith("content://")) {
            return true
        }
        if (uriString.startsWith("file://")) {
            val path = Uri.parse(uriString).path ?: return false
            return File(path).exists()
        }
        if (uriString.startsWith("/")) {
            return File(uriString).exists()
        }
        return true
    }

    fun toCoilModel(uriString: String?): Any? {
        if (uriString.isNullOrEmpty()) return null
        val resolved = resolveAvatarUrl(uriString) ?: return null
        if (resolved.startsWith("data:image/")) {
            try {
                val base64 = resolved.substringAfter("base64,")
                return android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                return null
            }
        }
        return resolved
    }

    fun toCoilRequest(context: Context, uriString: String?): Any? {
        if (uriString.isNullOrEmpty()) return null
        val resolved = resolveAvatarUrl(uriString) ?: return null
        if (resolved.startsWith("data:image/")) {
            return toCoilModel(resolved)
        }
        return coil.request.ImageRequest.Builder(context)
            .data(resolved)
            .decoderFactory(coil.decode.SvgDecoder.Factory())
            .crossfade(true)
            .build()
    }

    suspend fun uploadImageUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uriString = uri.toString()
            if (uriString.startsWith("http://") || uriString.startsWith("https://") || uriString.startsWith("/uploads/")) {
                return@withContext Result.success(resolveAvatarUrl(uriString) ?: uriString)
            }

            val bytes: ByteArray
            var mimeType = "image/jpeg"

            if (uriString.startsWith("data:image/")) {
                val base64Data = uriString.substringAfter("base64,")
                bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                if (uriString.contains("image/png")) mimeType = "image/png"
            } else {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Cannot open image file: $uri"))
                bytes = inputStream.use { it.readBytes() }
                mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            }

            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
            val filename = "avatar_${System.currentTimeMillis()}.$extension"
            val mediaType = mimeType.toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("file", filename, requestBody)

            val response = com.example.network.viora.VioraNetworkModule.api.uploadFile(part)
            if (response.success && response.data != null) {
                val fullUrl = resolveAvatarUrl(response.data.url) ?: response.data.url
                Result.success(fullUrl)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Failed to upload avatar" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUploadedFile(key: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = com.example.network.viora.VioraNetworkModule.api.deleteFile(key)
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
