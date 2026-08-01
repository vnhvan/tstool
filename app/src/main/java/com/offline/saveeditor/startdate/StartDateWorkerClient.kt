package com.offline.saveeditor.startdate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StartDateWorkerClient(private val context: Context) {
    data class Result(val epochSeconds: Long, val sha256: String, val backupPath: String? = null)

    suspend fun inspect(): Result = execute(StartDateWorkerProtocol.ACTION_INSPECT, null)
    suspend fun write(epochSeconds: Long): Result = execute(StartDateWorkerProtocol.ACTION_WRITE, epochSeconds)

    private suspend fun execute(action: String, value: Long?): Result = suspendCancellableCoroutine { continuation ->
        val receiver = object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (!continuation.isActive) return
                if (resultCode == StartDateWorkerProtocol.RESULT_OK) {
                    continuation.resume(Result(
                        epochSeconds = resultData.getLong(StartDateWorkerProtocol.KEY_VALUE),
                        sha256 = resultData.getString(StartDateWorkerProtocol.KEY_SHA256).orEmpty(),
                        backupPath = resultData.getString(StartDateWorkerProtocol.KEY_BACKUP),
                    ))
                } else {
                    continuation.resumeWithException(IllegalStateException(
                        resultData.getString(StartDateWorkerProtocol.KEY_MESSAGE) ?: "StartDate worker thất bại"
                    ))
                }
            }
        }
        val intent = Intent(context, StartDateWorkerService::class.java).apply {
            this.action = action
            putExtra(StartDateWorkerProtocol.EXTRA_RECEIVER, receiver)
            value?.let { putExtra(StartDateWorkerProtocol.EXTRA_VALUE, it) }
        }
        runCatching { context.startService(intent) }.onFailure { continuation.resumeWithException(it) }
    }
}
