package com.offline.saveeditor.root

import android.content.Context
import com.offline.saveeditor.util.sha256
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Direct root access for the Township save used by LDPlayer/rooted Android devices. */
class RootSaveAccess(private val context: Context) {
    data class WriteResult(
        val bytes: ByteArray,
        val rootBackupPath: String,
        val sha256: String,
    )

    fun readTownshipSave(forceStop: Boolean = true): ByteArray {
        val prefix = if (forceStop) "am force-stop $TOWNSHIP_PACKAGE >/dev/null 2>&1; " else ""
        val result = runRoot("${prefix}test -f ${shellQuote(SAVE_FILE)}; cat ${shellQuote(SAVE_FILE)}", 8)
        require(result.exitCode == 0) { result.errorMessage("Không đọc được mGameInfo.xml") }
        require(result.stdout.isNotEmpty()) { "mGameInfo.xml rỗng" }
        return result.stdout
    }

    fun readTownshipSha256(forceStop: Boolean = true): String {
        val prefix = if (forceStop) "am force-stop $TOWNSHIP_PACKAGE >/dev/null 2>&1; " else ""
        val command = prefix + "test -f ${shellQuote(SAVE_FILE)}; " +
            "(sha256sum ${shellQuote(SAVE_FILE)} 2>/dev/null || toybox sha256sum ${shellQuote(SAVE_FILE)}) | awk '{print \$1}'"
        val result = runRoot(command, 8)
        if (result.exitCode == 0) {
            val hash = result.stdout.toString(Charsets.UTF_8).trim().lineSequence().firstOrNull().orEmpty()
            if (hash.matches(Regex("[0-9a-fA-F]{64}"))) return hash.lowercase()
        }
        return sha256(readTownshipSave(forceStop = false))
    }

    fun writeTownshipSave(bytes: ByteArray): WriteResult {
        require(bytes.isNotEmpty()) { "Dữ liệu save mới rỗng" }
        val expectedHash = sha256(bytes)
        val temporary = File.createTempFile("mGameInfo_direct_", ".bin", context.cacheDir)
        try {
            temporary.writeBytes(bytes)
            val backupPath = "$SAVE_FILE.chucks.${System.currentTimeMillis()}.bak"
            val command = buildString {
                append("set -e; ")
                append("am force-stop $TOWNSHIP_PACKAGE >/dev/null 2>&1; ")
                append("test -f ${shellQuote(SAVE_FILE)}; ")
                append("cp -p ${shellQuote(SAVE_FILE)} ${shellQuote(backupPath)}; ")
                // Writing into the existing inode preserves owner, mode and SELinux context.
                append("cat ${shellQuote(temporary.absolutePath)} > ${shellQuote(SAVE_FILE)}; ")
                append("sync")
            }
            val write = runRoot(command, 12)
            require(write.exitCode == 0) { write.errorMessage("Không thể ghi trực tiếp save Township") }

            val verified = readTownshipSave(forceStop = false)
            if (sha256(verified) != expectedHash) {
                runRoot("cp -p ${shellQuote(backupPath)} ${shellQuote(SAVE_FILE)}; sync", 8)
                error("Xác minh SHA-256 thất bại; đã yêu cầu khôi phục bản root backup")
            }
            return WriteResult(verified, backupPath, expectedHash)
        } finally {
            temporary.delete()
        }
    }

    private data class CommandResult(
        val exitCode: Int,
        val stdout: ByteArray,
        val stderr: ByteArray,
        val invocation: String,
    ) {
        fun errorMessage(prefix: String): String {
            val detail = stderr.toString(Charsets.UTF_8).trim().ifBlank {
                stdout.toString(Charsets.UTF_8).trim()
            }
            return if (detail.isBlank()) "$prefix ($invocation, exit=$exitCode)" else "$prefix: $detail"
        }
    }

    private fun runRoot(command: String, timeoutSeconds: Long): CommandResult {
        val attempts = listOf(
            listOf("su", "0", "sh", "-c", command),
            listOf("su", "-c", command),
            listOf("su", "root", "sh", "-c", command),
        )
        var last: CommandResult? = null
        for (args in attempts) {
            val process = try { ProcessBuilder(args).start() } catch (_: Throwable) { continue }
            val executor = Executors.newFixedThreadPool(2)
            try {
                val stdoutFuture = executor.submit(Callable { process.inputStream.use { it.readBytes() } })
                val stderrFuture = executor.submit(Callable { process.errorStream.use { it.readBytes() } })
                val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
                if (!finished) {
                    process.destroyForcibly()
                    last = CommandResult(-1, byteArrayOf(), "Hết thời gian chờ root".toByteArray(), args.take(2).joinToString(" "))
                    continue
                }
                val result = CommandResult(
                    process.exitValue(),
                    stdoutFuture.get(2, TimeUnit.SECONDS),
                    stderrFuture.get(2, TimeUnit.SECONDS),
                    args.take(2).joinToString(" "),
                )
                if (result.exitCode == 0) return result
                last = result
            } finally {
                executor.shutdownNow()
            }
        }
        return last ?: CommandResult(-1, byteArrayOf(), "Không khởi chạy được su".toByteArray(), "su")
    }

    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    companion object {
        const val TOWNSHIP_PACKAGE = "com.playrix.township.vn"
        const val SAVE_FILE = "/data/data/$TOWNSHIP_PACKAGE/saves/mGameInfo.xml"
    }
}
