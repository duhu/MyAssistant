package com.myassistant.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.myassistant.app.data.util.DiagnosticLog
import com.myassistant.app.domain.usecase.AnalyzeScreenshotUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScreenshotAnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyzeScreenshotUseCase: AnalyzeScreenshotUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        DiagnosticLog.i("Worker 开始执行: attempt=$runAttemptCount")
        val imagePath = inputData.getString(KEY_IMAGE_PATH) ?: run {
            DiagnosticLog.e("Worker: 未收到图片路径")
            return Result.failure()
        }

        DiagnosticLog.i("Worker 分析: ${imagePath.takeLast(40)}")
        return try {
            val result = analyzeScreenshotUseCase(imagePath)
            result.fold(
                onSuccess = { card ->
                    DiagnosticLog.ok("✅ 分析成功: ${card.title}")
                    DiagnosticLog.i("卡片已保存: id=${card.id.take(8)}...")
                    Result.success()
                },
                onFailure = { e ->
                    DiagnosticLog.e("❌ 分析失败: ${e.message}")
                    if (runAttemptCount < MAX_RETRIES) {
                        DiagnosticLog.i("重试 ${runAttemptCount}/$MAX_RETRIES...")
                        Result.retry()
                    } else Result.failure()
                }
            )
        } catch (e: Exception) {
            DiagnosticLog.e("Worker 崩溃: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        const val KEY_IMAGE_PATH = "image_path"
        private const val MAX_RETRIES = 3
    }
}
