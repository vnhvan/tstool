package com.offline.saveeditor.storage

import android.content.Context
import com.offline.saveeditor.util.sha256
import com.offline.saveeditor.validation.ContainerInspection
import com.offline.saveeditor.validation.ContainerInspector
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupInfo(
    val fileName: String,
    val size: Long,
    val modifiedAtEpochMillis: Long,
    val sha256: String,
)

data class BackupCreateResult(val backup: BackupInfo, val created: Boolean)

data class BackupInspection(
    val backup: BackupInfo,
    val fileNameDigestMatches: Boolean,
    val container: ContainerInspection,
) {
    val isValid: Boolean get() = fileNameDigestMatches && container.validHeader && !container.isTruncated
}

class BackupStore(context: Context, private val capacity: Int = 25) : BackupRepository {
    private val directory = File(context.filesDir, "save_backups").apply {
        require(exists() || mkdirs()) { "Không thể tạo thư mục backup" }
        require(isDirectory) { "Đường dẫn backup không phải thư mục" }
    }

    override fun createVerified(container: ByteArray): BackupCreateResult {
        require(container.isNotEmpty()) { "Không thể sao lưu file rỗng" }
        val digest = sha256(container)
        list().firstOrNull { it.sha256 == digest }?.let { return BackupCreateResult(it, false) }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val file = File(directory, "mGameInfo_${stamp}_${digest.take(10)}.bak")
        val temporary = File(directory, ".${file.name}.tmp")
        temporary.outputStream().buffered().use { it.write(container); it.flush() }
        require(temporary.length() == container.size.toLong()) { "Kích thước backup ghi ra không khớp" }
        require(sha256(temporary) == digest) { "SHA-256 backup không khớp" }
        if (!temporary.renameTo(file)) {
            temporary.copyTo(file, overwrite = false)
            require(temporary.delete()) { "Không thể dọn file backup tạm" }
        }
        prune()
        return BackupCreateResult(file.toInfo(), true)
    }

    fun create(container: ByteArray): BackupInfo = createVerified(container).backup

    override fun list(): List<BackupInfo> = directory.listFiles().orEmpty()
        .filter { it.isFile && it.name.endsWith(".bak") }
        .sortedByDescending { it.lastModified() }
        .map { it.toInfo() }

    override fun inspect(fileName: String): BackupInspection {
        val file = safeFile(fileName)
        val bytes = file.readBytes()
        val info = file.toInfo()
        val expectedPrefix = file.nameWithoutExtension.substringAfterLast('_', "")
        return BackupInspection(
            backup = info,
            fileNameDigestMatches = expectedPrefix.length == 10 && info.sha256.startsWith(expectedPrefix),
            container = ContainerInspector.inspect(bytes),
        )
    }

    override fun read(fileName: String): ByteArray {
        val inspection = inspect(fileName)
        require(inspection.backup.size > 0) { "Backup rỗng" }
        require(inspection.fileNameDigestMatches) { "Backup không vượt qua kiểm tra SHA-256" }
        require(inspection.container.validHeader && !inspection.container.isTruncated) {
            inspection.container.message
        }
        return safeFile(fileName).readBytes()
    }

    override fun delete(fileName: String): Boolean = safeFile(fileName).delete()

    private fun prune() {
        directory.listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".bak") }
            .sortedByDescending { it.lastModified() }
            .drop(capacity)
            .forEach { it.delete() }
    }

    private fun safeFile(fileName: String): File {
        require(fileName == File(fileName).name) { "Tên backup không hợp lệ" }
        val file = File(directory, fileName)
        require(file.canonicalFile.parentFile == directory.canonicalFile) { "Đường dẫn backup không hợp lệ" }
        require(file.isFile) { "Không tìm thấy backup" }
        return file
    }

    private fun File.toInfo(): BackupInfo = BackupInfo(
        fileName = name,
        size = length(),
        modifiedAtEpochMillis = lastModified(),
        sha256 = sha256(this),
    )
}
