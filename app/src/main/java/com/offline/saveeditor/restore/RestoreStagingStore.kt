package com.offline.saveeditor.restore

import android.content.Context
import com.offline.saveeditor.storage.RestoreRepository
import java.io.File

data class StagedRestore(
    val fileName: String,
    val size: Long,
    val sha256: String,
    val sourceDescription: String,
)

class RestoreStagingStore(context: Context) : RestoreRepository {
    private val directory = File(context.filesDir, "restore_staging").apply {
        require(exists() || mkdirs()) { "Không thể tạo thư mục restore staging" }
        require(isDirectory) { "Đường dẫn restore staging không phải thư mục" }
    }

    override fun stage(plan: RestorePlan): StagedRestore {
        clear()
        val temporary = File(directory, ".mGameInfo.restore.tmp")
        val target = File(directory, "mGameInfo.restore")
        temporary.outputStream().buffered().use { stream ->
            stream.write(plan.bytes)
            stream.flush()
        }
        val verification = RestorePipeline.verifyWritten(plan, temporary.readBytes())
        require(verification.success) { verification.message }
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            require(temporary.delete()) { "Không thể xóa file staging tạm" }
        }
        val finalVerification = RestorePipeline.verifyWritten(plan, target.readBytes())
        require(finalVerification.success) { finalVerification.message }
        return StagedRestore(target.name, target.length(), plan.sha256, plan.sourceDescription)
    }

    override fun currentOrNull(): StagedRestore? {
        val target = File(directory, "mGameInfo.restore")
        if (!target.isFile || target.length() <= 0) return null
        val bytes = target.readBytes()
        val plan = runCatching { RestorePipeline.prepare(bytes, "restore staging") }.getOrNull() ?: return null
        return StagedRestore(target.name, target.length(), plan.sha256, plan.sourceDescription)
    }

    override fun readVerified(expectedSha256: String): ByteArray {
        val target = File(directory, "mGameInfo.restore")
        require(target.isFile) { "Chưa có file restore staging" }
        val bytes = target.readBytes()
        val plan = RestorePipeline.prepare(bytes, "restore staging")
        require(plan.sha256 == expectedSha256) { "SHA-256 staging không khớp bản đã chuẩn bị" }
        return bytes
    }

    override fun clear(): Boolean {
        var success = true
        directory.listFiles().orEmpty().forEach { if (!it.delete()) success = false }
        return success
    }
}
