package com.offline.saveeditor.startdate

import android.content.Context
import com.offline.saveeditor.codec.MGameInfoCodec
import com.offline.saveeditor.root.RootSaveAccess
import com.offline.saveeditor.util.sha256
import java.io.File

/** File-based pipeline for gameStartDate; isolated from the Compose/UI process. */
class StartDateFileProcessor(context: Context) {
    data class InspectResult(val epochSeconds: Long, val sha256: String)
    data class WriteResult(val epochSeconds: Long, val sha256: String, val backupPath: String)

    private val root = RootSaveAccess(context)
    private val workspace = File(context.filesDir, "start_date_worker_workspace").apply { mkdirs() }
    private val source = File(workspace, "source.mGameInfo")
    private val output = File(workspace, "output.mGameInfo")

    fun inspect(): InspectResult {
        root.copyTownshipSaveTo(source, forceStop = true)
        val container = source.readBytes()
        val xml = MGameInfoCodec.decode(container)
        return InspectResult(StartDateXmlBytes.read(xml), sha256(container))
    }

    fun write(epochSeconds: Long): WriteResult {
        root.copyTownshipSaveTo(source, forceStop = true)
        val original = source.readBytes()
        val xml = MGameInfoCodec.decode(original)
        val current = StartDateXmlBytes.read(xml)
        require(current != epochSeconds) { "Ngày tạo mới trùng giá trị hiện tại" }
        val editedXml = StartDateXmlBytes.replace(xml, epochSeconds)
        require(StartDateXmlBytes.read(editedXml) == epochSeconds) { "Không thay được gameStartDate" }
        val encoded = MGameInfoCodec.encode(
            xmlInput = editedXml,
            contentSeed = MGameInfoCodec.seedFromExisting(original),
            totalSize = original.size,
        )
        require(StartDateXmlBytes.read(MGameInfoCodec.decode(encoded)) == epochSeconds) {
            "Encode–decode kiểm tra gameStartDate thất bại"
        }
        output.writeBytes(encoded)
        val rootResult = root.replaceTownshipSaveFrom(output, expectedSha256 = sha256(encoded))
        return WriteResult(epochSeconds, rootResult.sha256, rootResult.rootBackupPath)
    }
}
