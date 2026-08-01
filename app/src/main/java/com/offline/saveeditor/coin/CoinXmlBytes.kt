package com.offline.saveeditor.coin

/** Minimal byte-level XML access for the single <Var name="money" ...> field. */
object CoinXmlBytes {
    private val varStart = "<Var".toByteArray()
    private val nameMoneyDouble = "name=\"money\"".toByteArray()
    private val nameMoneySingle = "name='money'".toByteArray()

    fun read(xml: ByteArray): Long {
        val tag = findMoneyTag(xml)
        val value = findValue(xml, tag.first, tag.last + 1)
        return xml.copyOfRange(value.first, value.last + 1).toString(Charsets.UTF_8).toLongOrNull()
            ?: error("Giá trị money không phải số")
    }

    fun replace(xml: ByteArray, newValue: Long): ByteArray {
        require(newValue in 0..2_000_000_000L) { "Coin phải trong khoảng 0..2.000.000.000" }
        val tag = findMoneyTag(xml)
        val value = findValue(xml, tag.first, tag.last + 1)
        val replacement = newValue.toString().toByteArray()
        val out = ByteArray(xml.size - (value.last - value.first + 1) + replacement.size)
        xml.copyInto(out, 0, 0, value.first)
        replacement.copyInto(out, value.first)
        xml.copyInto(out, value.first + replacement.size, value.last + 1, xml.size)
        return out
    }

    private fun findMoneyTag(xml: ByteArray): IntRange {
        var cursor = 0
        while (true) {
            val start = xml.indexOf(varStart, cursor)
            if (start < 0) error("Không tìm thấy biến money")
            val end = xml.indexOfByte('>'.code.toByte(), start + varStart.size)
            if (end < 0) error("Thẻ Var money bị lỗi")
            if (xml.contains(nameMoneyDouble, start, end + 1) || xml.contains(nameMoneySingle, start, end + 1)) {
                return start..end
            }
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
                        if (valueEnd < 0) error("Thuộc tính v của money bị lỗi")
                        return valueStart until valueEnd
                    }
                }
            }
            i++
        }
        error("Biến money không có thuộc tính v")
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
