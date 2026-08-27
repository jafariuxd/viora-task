package com.example

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun testWebp() {
    val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
    val bos = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.WEBP, 10, bos)
    val b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
    println("WEBP length: ${b64.length}")
}
