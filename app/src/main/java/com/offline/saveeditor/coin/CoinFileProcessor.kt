package com.offline.saveeditor.coin

import android.content.Context
import com.offline.saveeditor.codec.MGameInfoCodec
import com.offline.saveeditor.root.RootSaveAccess
import com.offline.saveeditor.util.sha256
import java.io.File

/** File-based Coin pipeline. It never constructs SaveDocument, analyzer trees, or full XML Strings. */
class CoinFileProcessor(context: Context) {
    data class InspectResult(val coin: Long, val sha256: String)
    data class WriteResult(val coin: Long, val sha256: String, val backupPath: String)

    private val root = RootSaveAccess(context)
    private val workspace = File(context.filesDir, "coin_worker_workspace").apply { mkdirs() }
    private val source = File(workspace, "source.mGameInfo")
    private val output = File(workspace, "output.mGameInfo")

    fun inspect(): InspectResult {
        root.copyTownshipSaveTo(source, forceStop = true)
        val container = source.readBytes()
        val xml = MGameInfoCodec.decode(container)
        return InspectResult(CoinXmlBytes.read(xml), sha256(container))
    }

    fun write(value: Long): WriteResult {
        root.copyTownshipSaveTo(source, forceStop = true)
        val original = source.readBytes()
        val xml = MGameInfoCodec.decode(original)
        val current = CoinXmlBytes.read(xml)
        require(current != value) { "Coin mới trùng Coin hiện tại" }
        val editedXml = CoinXmlBytes.replace(xml, value)
        require(CoinXmlBytes.read(editedXml) == value) { "Không thay được money trong XML" }
        val encoded = MGameInfoCodec.encode(
            xmlInput = editedXml,
            contentSeed = MGameInfoCodec.seedFromExisting(original),
            totalSize = original.size,
        )
        require(CoinXmlBytes.read(MGameInfoCodec.decode(encoded)) == value) { "Encode–decode kiểm tra Coin thất bại" }
        output.writeBytes(encoded)
        val rootResult = root.replaceTownshipSaveFrom(output, expectedSha256 = sha256(encoded))
        return WriteResult(value, rootResult.sha256, rootResult.rootBackupPath)
    }
}
