package com.offline.saveeditor.startdate

/** Minimal byte-level XML access for <Var name="gameStartDate" v="..."/>. */
object StartDateXmlBytes {
    private val varStart = "<Var".toByteArray()
    private val nameDouble = "name=\"gameStartDate\"".toByteArray()
    private val nameSingle = "name='gameStartDate'".toByteArray()

    fun read(xml: ByteArray): Long {
        val tag = findTag(xml)
        val value = findValue(xml, tag.first, tag.last + 1)
        return xml.copyOfRange(value.first, value.last + 1).toString(Charsets.UTF_8).toLongOrNull()
            ?: error("gameStartDate không phải Unix timestamp hợp lệ")
    }

    fun replace(xml: ByteArray, epochSeconds: Long): ByteArray {
        require(epochSeconds in 0..4_102_444_800L) { "Ngày tạo phải nằm trong khoảng 1970–2100" }
        val tag = findTag(xml)
        val value = findValue(xml, tag.first, tag.last + 1)
        val replacement = epochSeconds.toString().toByteArray()
        return ByteArray(xml.size - (value.last - value.first + 1) + replacement.size).also { out ->
            xml.copyInto(out, 0, 0, value.first)
            replacement.copyInto(out, value.first)
            xml.copyInto(out, value.first + replacement.size, value.last + 1, xml.size)
        }
    }

    private fun findTag(xml: ByteArray): IntRange {
        var cursor = 0
        while (true) {
            val start = xml.indexOf(varStart, cursor)
            if (start < 0) error("Không tìm thấy biến gameStartDate")
            val end = xml.indexOfByte('>'.code.toByte(), start + varStart.size)
            if (end < 0) error("Thẻ gameStartDate bị lỗi")
            if (xml.contains(nameDouble, start, end + 1) || xml.contains(nameSingle, start, end + 1)) return start..end
            cursor = end + 1
        }
    }

    private fun findValue(xml: ByteArray, start: Int, endExclusive: Int): IntRange {
        var i = start
        while (i + 3 < endExclusive) {
            if (xml[i] == 'v'.code.toByte()) {
                var j = i + 1
                while (j < endExclusive && xml[j].toInt().toChar().isWhitespace()) j++
                if (j < endExclusive && xml[j] == '='.code.toByte()) {
                    j++
                    while (j < endExclusive && xml[j].toInt().toChar().isWhitespace()) j++
                    if (j < endExclusive && (xml[j] == '"'.code.toByte() || xml[j] == '\''.code.toByte())) {
                        val quote = xml[j]
                        val valueStart = j + 1
                        val valueEnd = xml.indexOfByte(quote, valueStart, endExclusive)
                        if (valueEnd < 0) error("Thuộc tính v của gameStartDate bị lỗi")
                        return valueStart until valueEnd
                    }
                }
            }
            i++
        }
        error("gameStartDate không có thuộc tính v")
    }

    private fun ByteArray.indexOf(needle: ByteArray, start: Int): Int {
        if (needle.isEmpty()) return start.coerceAtMost(size)
        outer@ for (i in start.coerceAtLeast(0)..size - needle.size) {
            for (j in needle.indices) if (this[i + j] != needle[j]) continue@outer
            return i
        }
        return -1
    }

    private fun ByteArray.indexOfByte(value: Byte, start: Int, endExclusive: Int = size): Int {
        for (i in start.coerceAtLeast(0) until endExclusive.coerceAtMost(size)) if (this[i] == value) return i
        return -1
    }

    private fun ByteArray.contains(needle: ByteArray, start: Int, endExclusive: Int): Boolean {
        if (needle.isEmpty()) return true
        if (endExclusive - start < needle.size) return false
        outer@ for (i in start..endExclusive - needle.size) {
            for (j in needle.indices) if (this[i + j] != needle[j]) continue@outer
            return true
        }
        return false
    }
}
