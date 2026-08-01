package com.offline.saveeditor.crash

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashReporter(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val previous = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            val dir = File(context.filesDir, "crash_logs").apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val writer = StringWriter()
            throwable.printStackTrace(PrintWriter(writer))
            File(dir, "crash_$stamp.txt").writeText(buildString {
                appendLine("Township Offline Save Editor crash")
                appendLine("Thread: ${thread.name}")
                appendLine("Android: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
                appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine()
                append(writer.toString())
            })
            prune(dir, 10)
        }
        previous?.uncaughtException(thread, throwable)
    }

    fun list(): List<File> = File(context.filesDir, "crash_logs").listFiles().orEmpty()
        .filter { it.isFile && it.extension == "txt" }.sortedByDescending { it.lastModified() }

    fun read(fileName: String): ByteArray {
        require(fileName.matches(Regex("crash_[0-9_]+\\.txt"))) { "Tên crash log không hợp lệ" }
        val file = File(File(context.filesDir, "crash_logs"), fileName)
        require(file.canonicalFile.parentFile == File(context.filesDir, "crash_logs").canonicalFile) { "Đường dẫn không hợp lệ" }
        require(file.isFile) { "Không tìm thấy crash log" }
        return file.readBytes()
    }

    fun clear() { list().forEach { it.delete() } }

    companion object {
        fun install(context: Context) { Thread.setDefaultUncaughtExceptionHandler(CrashReporter(context.applicationContext)) }
        private fun prune(dir: File, capacity: Int) = dir.listFiles().orEmpty().sortedByDescending { it.lastModified() }.drop(capacity).forEach { it.delete() }
    }
}
