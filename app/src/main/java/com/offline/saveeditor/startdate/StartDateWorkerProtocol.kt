package com.offline.saveeditor.startdate

object StartDateWorkerProtocol {
    const val ACTION_INSPECT = "com.offline.saveeditor.startdate.INSPECT"
    const val ACTION_WRITE = "com.offline.saveeditor.startdate.WRITE"
    const val EXTRA_RECEIVER = "receiver"
    const val EXTRA_VALUE = "value"
    const val KEY_VALUE = "gameStartDate"
    const val KEY_SHA256 = "sha256"
    const val KEY_BACKUP = "backup"
    const val KEY_MESSAGE = "message"
    const val RESULT_OK = 1
    const val RESULT_ERROR = 2
}
