package com.offline.saveeditor.model

import com.offline.saveeditor.change.ChangeReport
import com.offline.saveeditor.codec.MGameInfoCodec
import com.offline.saveeditor.parser.SaveXmlEditor
import com.offline.saveeditor.util.sha256

object SaveRepository {
    const val MAX_INPUT_BYTES: Int = 64 * 1024 * 1024

    fun open(input: ByteArray): SaveDocument {
        require(input.isNotEmpty()) { "File rỗng" }
        require(input.size <= MAX_INPUT_BYTES) { "File vượt giới hạn ${MAX_INPUT_BYTES / (1024 * 1024)} MB" }
        val isXml = input.firstNonWhitespaceByte() == '<'.code.toByte()
        val xml = MGameInfoCodec.decode(input)
        return SaveDocument(
            container = input.copyOf(),
            xml = xml,
            contentSeed = if (isXml) null else MGameInfoCodec.seedFromExisting(input),
            containerSize = input.size,
            sha256 = sha256(input),
            fields = SaveXmlEditor.readFields(xml),
            sourceKind = if (isXml) SaveSourceKind.DECODED_XML else SaveSourceKind.BINARY_CONTAINER,
        )
    }

    fun encodeAndVerify(original: SaveDocument, editedXml: ByteArray): ByteArray {
        require(original.canEncode) { "XML đã giải mã chỉ dùng để đọc; hãy mở mGameInfo nhị phân để tạo save mới" }
        val encoded = MGameInfoCodec.encode(
            xmlInput = editedXml,
            contentSeed = requireNotNull(original.contentSeed),
            totalSize = original.containerSize,
        )
        val decodedAgain = MGameInfoCodec.decode(encoded)
        require(decodedAgain.contentEquals(trimToRoot(editedXml))) {
            "Xác minh round-trip thất bại; không xuất file"
        }
        return encoded
    }

    fun previewChanges(original: SaveDocument, editedXml: ByteArray): ChangeReport =
        ChangeReport.between(original.fields, SaveXmlEditor.readFields(editedXml))

    fun backupName(originalSha256: String): String =
        "mGameInfo_backup_${originalSha256.take(12)}.xml"

    private fun ByteArray.firstNonWhitespaceByte(): Byte? = firstOrNull { !it.toInt().toChar().isWhitespace() }

    private fun trimToRoot(xml: ByteArray): ByteArray {
        val marker = "</root>".toByteArray()
        outer@ for (i in 0..xml.size - marker.size) {
            for (j in marker.indices) if (xml[i + j] != marker[j]) continue@outer
            return xml.copyOfRange(0, i + marker.size)
        }
        error("XML không chứa </root>")
    }
}
