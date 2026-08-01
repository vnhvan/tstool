package com.offline.saveeditor.coin

object CoinWorkerProtocol {
    const val ACTION_INSPECT = "com.offline.saveeditor.coin.INSPECT"
    const val ACTION_WRITE = "com.offline.saveeditor.coin.WRITE"
    const val EXTRA_RECEIVER = "receiver"
    const val EXTRA_VALUE = "value"
    const val RESULT_OK = 1
    const val RESULT_ERROR = 2
    const val KEY_COIN = "coin"
    const val KEY_SHA256 = "sha256"
    const val KEY_BACKUP = "backup"
    const val KEY_MESSAGE = "message"
}
