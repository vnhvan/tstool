package com.offline.saveeditor.restore

import com.offline.saveeditor.util.sha256
import com.offline.saveeditor.validation.ContainerInspector

data class RestorePlan(
    val bytes: ByteArray,
    val sha256: String,
    val size: Int,
    val sourceDescription: String,
    val verificationMessage: String,
)

data class RestoreVerification(
    val success: Boolean,
    val expectedSha256: String,
    val actualSha256: String?,
    val expectedSize: Int,
    val actualSize: Int?,
    val message: String,
)

object RestorePipeline {
    fun prepare(bytes: ByteArray, sourceDescription: String): RestorePlan {
        require(bytes.isNotEmpty()) { "Không thể chuẩn bị restore từ file rỗng" }
        val inspection = ContainerInspector.inspect(bytes)
        require(inspection.validHeader && !inspection.isTruncated) { inspection.message }
        return RestorePlan(
            bytes = bytes.copyOf(),
            sha256 = sha256(bytes),
            size = bytes.size,
            sourceDescription = sourceDescription,
            verificationMessage = inspection.message,
        )
    }

    fun verifyWritten(plan: RestorePlan, writtenBytes: ByteArray?): RestoreVerification {
        if (writtenBytes == null) return RestoreVerification(false, plan.sha256, null, plan.size, null, "Không đọc lại được file đã ghi")
        val actualHash = sha256(writtenBytes)
        val ok = writtenBytes.size == plan.size && actualHash == plan.sha256
        return RestoreVerification(
            success = ok,
            expectedSha256 = plan.sha256,
            actualSha256 = actualHash,
            expectedSize = plan.size,
            actualSize = writtenBytes.size,
            message = if (ok) "Restore staging đã xác minh byte-for-byte" else "File staging không khớp kích thước hoặc SHA-256",
        )
    }
}
