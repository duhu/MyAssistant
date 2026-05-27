package com.myassistant.app.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun compressToBase64(path: String, maxSizeKB: Int = 800): Result<String> = runCatching {
        val file = File(path)
        DiagnosticLog.i("压缩: ${path.takeLast(40)} (${file.length()} bytes)")

        if (!file.exists() || file.length() == 0L) {
            error("文件不存在或为空: $path")
        }

        val bytes = compress(path, maxSizeKB)
        if (bytes.isEmpty()) {
            error("压缩结果为空")
        }

        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        DiagnosticLog.ok("压缩完成: ${b64.length} chars (${bytes.size / 1024}KB)")
        b64
    }

    fun compress(path: String, maxSizeKB: Int = 800): ByteArray {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            DiagnosticLog.e("解码失败: 无法读取图片尺寸 (${options.outWidth}x${options.outHeight})")
            return ByteArray(0)
        }

        options.inSampleSize = calculateInSampleSize(options, 1280, 1280)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(path, options)
            ?: run {
                DiagnosticLog.e("解码失败: BitmapFactory 返回 null")
                return ByteArray(0)
            }

        val scaled = scaleToMax(bitmap, 1280)

        var quality = 85
        var output: ByteArray
        do {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            output = out.toByteArray()
            quality -= 10
        } while (output.size > maxSizeKB * 1024 && quality > 30)

        if (bitmap !== scaled) bitmap.recycle()
        scaled.recycle()
        return output
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleToMax(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
