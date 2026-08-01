package com.offline.saveeditor.startdate

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import kotlin.concurrent.thread

class StartDateWorkerService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val safeIntent = intent ?: return START_NOT_STICKY
        @Suppress("DEPRECATION")
        val receiver = safeIntent.getParcelableExtra(StartDateWorkerProtocol.EXTRA_RECEIVER) as? ResultReceiver
            ?: return START_NOT_STICKY
        thread(name = "start-date-file-worker") {
            try {
                val processor = StartDateFileProcessor(applicationContext)
                val result = when (safeIntent.action) {
                    StartDateWorkerProtocol.ACTION_INSPECT -> processor.inspect().let {
                        Bundle().apply {
                            putLong(StartDateWorkerProtocol.KEY_VALUE, it.epochSeconds)
                            putString(StartDateWorkerProtocol.KEY_SHA256, it.sha256)
                        }
                    }
                    StartDateWorkerProtocol.ACTION_WRITE -> processor.write(
                        safeIntent.getLongExtra(StartDateWorkerProtocol.EXTRA_VALUE, -1L)
                    ).let {
                        Bundle().apply {
                            putLong(StartDateWorkerProtocol.KEY_VALUE, it.epochSeconds)
                            putString(StartDateWorkerProtocol.KEY_SHA256, it.sha256)
                            putString(StartDateWorkerProtocol.KEY_BACKUP, it.backupPath)
                        }
                    }
                    else -> error("Yêu cầu StartDate worker không hợp lệ")
                }
                receiver.send(StartDateWorkerProtocol.RESULT_OK, result)
            } catch (error: Throwable) {
                receiver.send(StartDateWorkerProtocol.RESULT_ERROR, Bundle().apply {
                    putString(StartDateWorkerProtocol.KEY_MESSAGE, error.message ?: error.javaClass.simpleName)
                })
            } finally {
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }
}
