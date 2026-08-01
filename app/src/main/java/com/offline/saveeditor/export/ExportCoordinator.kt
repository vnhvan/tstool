package com.offline.saveeditor.export

import com.offline.saveeditor.change.ChangeReport

/** Pure export model; Android document launchers only consume these payloads. */
enum class ExportKind(val mimeType: String) {
    BINARY("application/octet-stream"),
    XML("text/xml"),
    TEXT("text/plain"),
}

data class ExportAudit(
    val sourceSha256: String,
    val report: ChangeReport,
)

data class ExportPayload(
    val bytes: ByteArray,
    val requestedName: String,
    val kind: ExportKind,
    val audit: ExportAudit? = null,
) {
    val safeName: String = ExportNames.safeFileName(requestedName)

    init {
        require(bytes.isNotEmpty()) { "Không thể xuất dữ liệu rỗng" }
        require(safeName.isNotBlank()) { "Tên file xuất không hợp lệ" }
    }
}
