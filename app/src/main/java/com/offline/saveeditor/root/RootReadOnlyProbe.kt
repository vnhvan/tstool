package com.offline.saveeditor.root

import java.io.File
import java.util.concurrent.TimeUnit

data class RootProbeResult(
    val available: Boolean,
    val townshipSaveDirectoryReadable: Boolean,
    val files: List<String>,
    val message: String,
)

object RootReadOnlyProbe {
    const val DEFAULT_SAVE_DIR = "/data/data/com.playrix.township.vn/saves"

    fun probe(saveDirectory: String = DEFAULT_SAVE_DIR, timeoutSeconds: Long = 3): RootProbeResult {
        val direct = File(saveDirectory)
        if (direct.isDirectory && direct.canRead()) {
            return RootProbeResult(true, true, direct.list().orEmpty().sorted(), "Đọc được trực tiếp thư mục saves")
        }
        return runCatching {
            val process = ProcessBuilder("su", "-c", "ls -1 '${saveDirectory.replace("'", "'\\''")}' 2>/dev/null")
                .redirectErrorStream(true).start()
            val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                RootProbeResult(false, false, emptyList(), "Root probe hết thời gian")
            } else {
                val output = process.inputStream.bufferedReader().readLines().filter { it.isNotBlank() }
                val ok = process.exitValue() == 0
                RootProbeResult(ok, ok, if (ok) output.sorted() else emptyList(), if (ok) "Root khả dụng; chỉ đọc được thư mục saves" else "Không có quyền root hoặc thư mục không đọc được")
            }
        }.getOrElse { RootProbeResult(false, false, emptyList(), "Root probe lỗi: ${it.message}") }
    }
}
