package com.offline.saveeditor.workspace

import android.content.Context
import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.model.SaveRepository
import com.offline.saveeditor.root.RootSaveAccess
import com.offline.saveeditor.util.sha256
import java.io.File

/** Reuses decoded XML while the Township source save SHA-256 is unchanged. */
class CoinWorkspaceStore(context: Context) {
    data class LoadResult(val document: SaveDocument, val reused: Boolean)

    private val dir = File(context.filesDir, "coin_workspace")
    private val sourceFile = File(dir, "source.mGameInfo")
    private val xmlFile = File(dir, "working.xml")
    private val hashFile = File(dir, "source.sha256")

    fun load(access: RootSaveAccess): LoadResult {
        dir.mkdirs()
        val currentHash = access.readTownshipSha256(forceStop = true)
        val cachedHash = hashFile.takeIf(File::isFile)?.readText()?.trim()

        if (cachedHash == currentHash && sourceFile.isFile && xmlFile.isFile) {
            val source = sourceFile.readBytes()
            val xml = xmlFile.readBytes()
            if (source.isNotEmpty() && xml.isNotEmpty() && sha256(source) == currentHash) {
                return LoadResult(SaveRepository.openCached(source, xml), reused = true)
            }
            clear()
        }

        val source = access.readTownshipSave(forceStop = false)
        require(sha256(source) == currentHash) { "Save thay đổi trong lúc tạo workspace; hãy thử lại" }
        val document = SaveRepository.open(source)
        save(document)
        return LoadResult(document, reused = false)
    }

    fun save(document: SaveDocument) {
        require(document.canEncode) { "Chỉ lưu workspace từ mGameInfo nhị phân" }
        dir.mkdirs()
        atomicWrite(sourceFile, document.container)
        atomicWrite(xmlFile, document.xml)
        atomicWrite(hashFile, document.sha256.toByteArray())
    }

    fun clear() {
        sourceFile.delete()
        xmlFile.delete()
        hashFile.delete()
    }

    private fun atomicWrite(target: File, bytes: ByteArray) {
        val temporary = File(target.parentFile, target.name + ".tmp")
        temporary.outputStream().use { output ->
            output.write(bytes)
            output.fd.sync()
        }
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }
    }
}
