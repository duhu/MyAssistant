package com.myassistant.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.myassistant.app.R
import com.myassistant.app.data.util.DiagnosticLog
import com.myassistant.app.worker.ScreenshotAnalysisWorker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotMonitorService : Service() {

    @Inject lateinit var workManager: WorkManager

    private var handlerThread: HandlerThread? = null
    private var screenshotObserver: ScreenshotObserver? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        DiagnosticLog.i("监控服务 onCreate")

        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification())
            DiagnosticLog.ok("前台通知已显示")
        } catch (e: Exception) {
            DiagnosticLog.e("startForeground 失败: ${e.message}")
            stopSelf()
            return
        }

        try {
            startObserving()
        } catch (e: Exception) {
            DiagnosticLog.e("Observer 注册失败: ${e.message}")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        DiagnosticLog.i("监控服务 onDestroy")
        screenshotObserver?.let {
            try { contentResolver.unregisterContentObserver(it) }
            catch (e: Exception) { DiagnosticLog.e("unregister 失败: ${e.message}") }
        }
        handlerThread?.quitSafely()
        super.onDestroy()
    }

    private fun startObserving() {
        handlerThread = HandlerThread("ScreenshotObserverThread").also { it.start() }
        val observer = ScreenshotObserver(
            contentResolver = contentResolver,
            handler = Handler(handlerThread!!.looper),
            onScreenshotDetected = { uri -> onNewImage(uri) },
            onAnyChange = { msg -> DiagnosticLog.i(msg) }
        )
        screenshotObserver = observer
        contentResolver.registerContentObserver(
            ScreenshotObserver.MEDIA_URI,
            true,
            observer
        )
        DiagnosticLog.ok("ContentObserver 已注册 (v5)")
    }

    private fun onNewImage(uri: Uri) {
        val id = uri.lastPathSegment
        DiagnosticLog.ok("📸 截屏: $id")
        waitAndCopy(uri, 0)
    }

    private fun waitAndCopy(uri: Uri, attempt: Int) {
        if (attempt >= RETRY_DELAYS_MS.size) {
            DiagnosticLog.e("放弃: ${RETRY_DELAYS_MS.size} 次重试仍无法读取")
            return
        }

        val delay = RETRY_DELAYS_MS[attempt]
        if (attempt > 0) {
            DiagnosticLog.i("等待 ${delay}ms (${attempt + 1}/${RETRY_DELAYS_MS.size})...")
        }

        mainHandler.postDelayed({
            val file = copyUriToCache(uri)
            if (file != null) {
                enqueueAnalysis(file)
            } else {
                waitAndCopy(uri, attempt + 1)
            }
        }, delay.toLong())
    }

    private fun copyUriToCache(uri: Uri): File? {
        val cachedFile = File(cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")

        val success = runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        }.getOrElse { e ->
            DiagnosticLog.e("流复制失败: ${e.message?.take(60)}")
            false
        }

        if (!success || !cachedFile.exists() || cachedFile.length() == 0L) {
            cachedFile.delete()
            return null
        }

        DiagnosticLog.ok("缓存成功: ${cachedFile.length()} bytes → ${cachedFile.absolutePath.takeLast(40)}")
        return cachedFile
    }

    private fun enqueueAnalysis(file: File) {
        val workRequest = OneTimeWorkRequestBuilder<ScreenshotAnalysisWorker>()
            .setInputData(workDataOf(ScreenshotAnalysisWorker.KEY_IMAGE_PATH to file.absolutePath))
            .addTag("screenshot_analysis")
            .build()

        DiagnosticLog.i("Worker 入队: ${workRequest.id}")
        workManager.enqueue(workRequest)
        DiagnosticLog.i("WorkManager 已调度")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.monitor_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.monitor_notification_title))
            .setContentText(getString(R.string.monitor_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "screenshot_monitor"
        // 3s, 5s, 8s — generous delays for MediaStore write
        private val RETRY_DELAYS_MS = intArrayOf(3000, 5000, 8000)
    }
}
