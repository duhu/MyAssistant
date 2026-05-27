package com.myassistant.app.service

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log

class ScreenshotObserver(
    private val contentResolver: ContentResolver,
    handler: Handler,
    private val onScreenshotDetected: (Uri) -> Unit,
    private val onAnyChange: (String) -> Unit = {}
) : ContentObserver(handler) {

    private var lastProcessedId: Long = -1

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        onAnyChange("onChange: selfChange=$selfChange, uri=${uri?.lastPathSegment ?: "null"}")

        // Strategy 1: Use the onChange URI directly if it has a numeric ID
        val idFromUri = uri?.lastPathSegment?.toLongOrNull()
        if (idFromUri != null && idFromUri != lastProcessedId) {
            onAnyChange("尝试直接使用 URI id=$idFromUri")
            val imageUri = Uri.withAppendedPath(MEDIA_URI, idFromUri.toString())
            if (checkAndProcess(imageUri, idFromUri)) return
            onAnyChange("直接查询失败，延迟重试...")
        }

        // Strategy 2: Query latest image from MediaStore
        val latestUri = getLatestImageUri()
        if (latestUri != null) {
            val id = latestUri.lastPathSegment?.toLongOrNull() ?: -1
            if (id == lastProcessedId) {
                onAnyChange("跳过重复 id=$id")
                return
            }
            if (checkAndProcess(latestUri, id)) return
            onAnyChange("最新图查询失败，最后手段: 延迟重试...")
        }

        // Strategy 3: Delayed retry (MediaStore may need time to index)
        retryWithDelay(idFromUri)
    }

    private fun retryWithDelay(hintId: Long?) {
        Handler(Looper.getMainLooper()).postDelayed({
            Thread {
                if (hintId != null) {
                    val uri = Uri.withAppendedPath(MEDIA_URI, hintId.toString())
                    if (checkAndProcess(uri, hintId)) return@Thread
                }
                val latestUri = getLatestImageUri()
                if (latestUri != null) {
                    val id = latestUri.lastPathSegment?.toLongOrNull() ?: -1
                    if (id != lastProcessedId) checkAndProcess(latestUri, id)
                }
            }.start()
        }, 800) // 800ms delay for MediaStore indexing
    }

    private fun checkAndProcess(uri: Uri, id: Long): Boolean {
        val isScreenshot = isScreenshotUri(uri)
        onAnyChange("检测图片: id=$id, isScreenshot=$isScreenshot, name=${getDisplayName(uri)}, path=${getDataPath(uri).takeLast(40)}")
        if (isScreenshot) {
            lastProcessedId = id
            onScreenshotDetected(uri)
            return true
        }
        return false
    }

    private fun getLatestImageUri(): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val bundle = Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_LIMIT, 1)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_ADDED))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
        }
        return runCatching {
            contentResolver.query(
                MEDIA_URI, projection, bundle, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    Uri.withAppendedPath(MEDIA_URI, id.toString())
                } else null
            }
        }.getOrNull()
    }

    private fun isScreenshotUri(uri: Uri): Boolean {
        val name = getDisplayName(uri).lowercase()
        val path = getDataPath(uri).lowercase()
        if (name.contains("screenshot") || name.contains("截屏") || name.contains("截图")) return true
        if (path.contains("screenshot") || path.contains("截屏") || path.contains("截图")) return true
        if (path.contains("/pictures/screenshots") || path.contains("/dcim/screenshots")) return true
        return false
    }

    private fun getDisplayName(uri: Uri): String {
        return runCatching {
            contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) ?: "" else ""
            } ?: ""
        }.getOrDefault(uri.lastPathSegment ?: "")
    }

    private fun getDataPath(uri: Uri): String {
        return runCatching {
            contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) ?: "" else ""
            } ?: ""
        }.getOrDefault("")
    }

    companion object {
        val MEDIA_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}
