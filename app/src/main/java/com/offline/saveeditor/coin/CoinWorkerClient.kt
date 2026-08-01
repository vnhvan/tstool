package com.offline.saveeditor.coin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoinWorkerClient(private val context: Context) {
    data class Result(val coin: Long, val sha256: String, val backupPath: String? = null)

    suspend fun inspect(): Result = execute(CoinWorkerProtocol.ACTION_INSPECT, null)
    suspend fun write(value: Long): Result = execute(CoinWorkerProtocol.ACTION_WRITE, value)

    private suspend fun execute(action: String, value: Long?): Result = suspendCancellableCoroutine { continuation ->
        val receiver = object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                if (!continuation.isActive) return
                if (resultCode == CoinWorkerProtocol.RESULT_OK) {
                    continuation.resume(Result(
                        coin = resultData.getLong(CoinWorkerProtocol.KEY_COIN),
                        sha256 = resultData.getString(CoinWorkerProtocol.KEY_SHA256).orEmpty(),
                        backupPath = resultData.getString(CoinWorkerProtocol.KEY_BACKUP),
                    ))
                } else {
                    continuation.resumeWithException(IllegalStateException(
                        resultData.getString(CoinWorkerProtocol.KEY_MESSAGE) ?: "Coin worker thất bại"
                    ))
                }
            }
        }
        val intent = Intent(context, CoinWorkerService::class.java).apply {
            this.action = action
            putExtra(CoinWorkerProtocol.EXTRA_RECEIVER, receiver)
            value?.let { putExtra(CoinWorkerProtocol.EXTRA_VALUE, it) }
        }
        runCatching { context.startService(intent) }.onFailure { continuation.resumeWithException(it) }
    }
}
