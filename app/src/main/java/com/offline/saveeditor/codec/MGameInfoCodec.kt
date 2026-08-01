package com.offline.saveeditor.codec

import net.jpountz.lz4.LZ4Factory
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MGameInfoCodec {
    private const val MASK32 = 0xFFFF_FFFFL
    private const val FORMAT_79 = 0x79
    private const val HASH_TABLE_SIZE = 727
    private const val MAX_XML_SIZE = 32 * 1024 * 1024
    private const val MAX_CONTAINER_SIZE = 64 * 1024 * 1024
    private val LZ4_MAGIC = byteArrayOf(0x04, 0x22, 0x4D, 0x18)
    private val XML_END = "</root>".toByteArray()

    fun decode(container: ByteArray): ByteArray {
        require(container.isNotEmpty()) { "File rỗng" }
        require(container.size <= MAX_CONTAINER_SIZE) { "File vượt giới hạn an toàn 64 MB" }
        val firstContent = container.indexOfFirst { !it.toInt().toChar().isWhitespace() }
        if (firstContent >= 0 && container[firstContent] == '<'.code.toByte()) {
            val end = container.indexOf(XML_END, firstContent)
            require(end >= 0) { "XML không chứa </root>" }
            return container.copyOfRange(firstContent, end + XML_END.size)
        }
        require(container.size >= 8) { "Container shorter than header" }
        val payload = xorDecode(container)
        val decoded = if (payload.startsWith(LZ4_MAGIC)) {
            require(payload.size >= 8) { "Truncated LZ4 payload" }
            val outputSize = leInt(payload, 4)
            require(outputSize in 1..MAX_XML_SIZE) { "Kích thước XML giải nén không hợp lệ: $outputSize" }
            val out = ByteArray(outputSize)
            LZ4Factory.fastestInstance().fastDecompressor()
                .decompress(payload, 8, out, 0, outputSize)
            out
        } else payload
        val end = decoded.indexOf(XML_END)
        require(end >= 0) { "Decoded payload does not contain </root>" }
        return decoded.copyOfRange(0, end + XML_END.size)
    }

    fun encode(xmlInput: ByteArray, contentSeed: Long, totalSize: Int? = null): ByteArray {
        val end = xmlInput.indexOf(XML_END)
        require(end >= 0) { "Input does not contain </root>" }
        val xml = xmlInput.copyOfRange(0, end + XML_END.size)
        require(xml.size <= MAX_XML_SIZE) { "XML vượt giới hạn an toàn 32 MB" }
        val raw = xml + byteArrayOf(0)
        val compressor = LZ4Factory.fastestInstance().fastCompressor()
        val max = compressor.maxCompressedLength(raw.size)
        val compressed = ByteArray(max)
        val compressedSize = compressor.compress(raw, 0, raw.size, compressed, 0, max)
        val payload = LZ4_MAGIC + leBytes(raw.size) + compressed.copyOf(compressedSize)
        return xorEncode(payload, contentSeed, totalSize ?: payload.size + 8)
    }

    fun seedFromExisting(container: ByteArray): Long {
        require(container.size >= 8 && (container[0].toInt() and 0xFF) == FORMAT_79)
        return leUInt(container, 4)
    }

    private fun xorDecode(container: ByteArray): ByteArray {
        require(container.size >= 8) { "Container shorter than header" }
        require((container[0].toInt() and 0xFF) == FORMAT_79) { "Unsupported format" }
        val headerSeed = (container[1].toLong() and 0xFF) or
            ((container[2].toLong() and 0xFF) shl 8) or
            ((container[3].toLong() and 0xFF) shl 16)
        val contentSeed = leUInt(container, 4)
        val recovered = u32(headerSeed - (810733L xor container.size.toLong())) xor 235176L
        require(recovered <= (container.size - 8).toLong()) { "Payload length vượt kích thước container" }
        val length = recovered.toInt()
        require(length >= 0)
        val table = buildHashTable(headerSeed, u32(contentSeed + 4))
        val out = container.copyOfRange(8, 8 + length)
        var ti = 0
        for (i in out.indices) {
            if (i > 0) out[i] = ((out[i].toInt() - out[i - 1].toInt()) and 0xFF).toByte()
            out[i] = (out[i].toInt() xor (table[ti].toInt() and 0xFF)).toByte()
            ti = (ti + 1) % HASH_TABLE_SIZE
        }
        return out
    }

    private fun xorEncode(payload: ByteArray, contentSeed: Long, totalSize: Int): ByteArray {
        require(totalSize >= payload.size + 8) { "Container đích quá nhỏ cho payload đã nén" }
        val headerSeed = u32((payload.size.toLong() xor 235176L) + (totalSize.toLong() xor 810733L))
        require(headerSeed <= 0xFF_FFFFL)
        val table = buildHashTable(headerSeed, u32(contentSeed + 4))
        val out = ByteArray(totalSize)
        out[0] = FORMAT_79.toByte()
        out[1] = headerSeed.toByte(); out[2] = (headerSeed shr 8).toByte(); out[3] = (headerSeed shr 16).toByte()
        val seedBytes = leBytes(contentSeed.toInt())
        seedBytes.copyInto(out, 4)
        var previousPlain = 0
        payload.forEachIndexed { i, b ->
            val value = b.toInt() and 0xFF
            out[8 + i] = (((value xor (table[i % HASH_TABLE_SIZE].toInt() and 0xFF)) + previousPlain) and 0xFF).toByte()
            previousPlain = value
        }
        return out
    }

    private fun buildHashTable(headerSeed: Long, contentSeedPlusFour: Long): ByteArray {
        var state = u32(contentSeedPlusFour)
        val table = ByteArray(HASH_TABLE_SIZE)
        var pos = 0
        while (pos < table.size) {
            state = murmurHash2(leBytes(state.toInt()), 4, headerSeed)
            val bytes = leBytes(state.toInt())
            for (i in bytes.indices) if (pos + i < table.size) table[pos + i] = bytes[i]
            pos += 4
        }
        return table
    }

    private fun murmurHash2(data: ByteArray, length: Int, seed: Long): Long {
        val m = 0x5BD1E995L
        var h = u32(seed xor length.toLong())
        var offset = 0
        var remaining = length
        while (remaining >= 4) {
            var k = leUInt(data, offset)
            k = u32(k * m); k = k xor (k shr 24); k = u32(k * m)
            h = u32(h * m); h = u32(h xor k)
            offset += 4; remaining -= 4
        }
        if (remaining >= 3) h = h xor ((data[offset + 2].toLong() and 0xFF) shl 16)
        if (remaining >= 2) h = h xor ((data[offset + 1].toLong() and 0xFF) shl 8)
        if (remaining >= 1) { h = h xor (data[offset].toLong() and 0xFF); h = u32(h * m) }
        h = h xor (h shr 13); h = u32(h * m); h = h xor (h shr 15)
        return u32(h)
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean = size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
    private fun ByteArray.indexOf(needle: ByteArray, startIndex: Int = 0): Int {
        if (needle.isEmpty() || startIndex < 0 || startIndex > size - needle.size) return -1
        outer@ for (i in startIndex..size - needle.size) {
            for (j in needle.indices) if (this[i + j] != needle[j]) continue@outer
            return i
        }
        return -1
    }
    private fun leUInt(data: ByteArray, offset: Int): Long = leInt(data, offset).toLong() and MASK32
    private fun leInt(data: ByteArray, offset: Int): Int = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
    private fun leBytes(value: Int): ByteArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    private fun u32(value: Long): Long = value and MASK32
}
