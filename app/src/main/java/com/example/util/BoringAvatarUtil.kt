package com.example.util

import android.content.Context
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import java.net.URLEncoder

object BoringAvatarUtil {
    val COLORS = listOf("f5f5f5","aaff00","ababab","aaff00","474747")
    val COLORS_PARAM = COLORS.joinToString("&colors=%23", prefix = "colors=%23")

    fun getAvatarUrl(seed: String, variant: String = "beam"): String {
        val cleanSeed = seed.trim()
            .lowercase()
            .removePrefix("@")
            .let { s ->
                if (s.startsWith("content:") || s.startsWith("file:") || s.startsWith("/")) {
                    "user"
                } else {
                    s
                }
            }
            .replace("\\s+".toRegex(), "")
            .ifBlank { "user" }

        val encodedSeed = URLEncoder.encode(cleanSeed, "UTF-8")
        return "https://boring-avatars-api.vercel.app/api/avatar?variant=$variant&name=$encodedSeed&$COLORS_PARAM"
    }

    fun getCoilRequest(context: Context, url: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build()
    }
}



