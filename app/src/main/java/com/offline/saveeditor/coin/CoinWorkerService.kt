package com.offline.saveeditor.coin

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import kotlin.concurrent.thread

/** Isolated process worker so codec failure or memory pressure does not terminate the Compose UI process. */
class CoinWorkerService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val safeIntent = intent ?: return START_NOT_STICKY
        @Suppress("DEPRECATION")
        val receiver = safeIntent.getParcelableExtra(CoinWorkerProtocol.EXTRA_RECEIVER) as? ResultReceiver
            ?: return START_NOT_STICKY
        thread(name = "coin-file-worker") {
            try {
                val processor = CoinFileProcessor(applicationContext)
                val result = when (safeIntent.action) {
                    CoinWorkerProtocol.ACTION_INSPECT -> {
                        val inspected = processor.inspect()
                        Bundle().apply {
                            putLong(CoinWorkerProtocol.KEY_COIN, inspected.coin)
                            putString(CoinWorkerProtocol.KEY_SHA256, inspected.sha256)
                        }
                    }
                    CoinWorkerProtocol.ACTION_WRITE -> {
                        val written = processor.write(safeIntent.getLongExtra(CoinWorkerProtocol.EXTRA_VALUE, -1L))
                        Bundle().apply {
                            putLong(CoinWorkerProtocol.KEY_COIN, written.coin)
                            putString(CoinWorkerProtocol.KEY_SHA256, written.sha256)
                            putString(CoinWorkerProtocol.KEY_BACKUP, written.backupPath)
                        }
                    }
                    else -> error("Yêu cầu Coin worker không hợp lệ")
                }
                receiver.send(CoinWorkerProtocol.RESULT_OK, result)
            } catch (error: Throwable) {
                receiver.send(CoinWorkerProtocol.RESULT_ERROR, Bundle().apply {
                    putString(CoinWorkerProtocol.KEY_MESSAGE, error.message ?: error.javaClass.simpleName)
                })
            } finally {
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }
}
