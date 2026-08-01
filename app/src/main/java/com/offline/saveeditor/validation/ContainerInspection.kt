package com.offline.saveeditor.validation

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class ContainerKind { XML, BINARY_79, UNKNOWN }

data class ContainerInspection(
    val kind: ContainerKind,
    val validHeader: Boolean,
    val declaredPayloadSize: Int?,
    val availablePayloadSize: Int,
    val isTruncated: Boolean,
    val message: String,
)

object ContainerInspector {
    private const val FORMAT_79 = 0x79
    private const val MIN_HEADER_SIZE = 8

    fun inspect(input: ByteArray): ContainerInspection {
        if (input.isEmpty()) return ContainerInspection(
            ContainerKind.UNKNOWN, false, null, 0, true, "File rỗng",
        )

        val first = input.firstOrNull { !it.toInt().toChar().isWhitespace() }
        if (first == '<'.code.toByte()) {
            val hasRootEnd = input.indexOf("</root>".toByteArray()) >= 0
            return ContainerInspection(
                kind = ContainerKind.XML,
                validHeader = hasRootEnd,
                declaredPayloadSize = null,
                availablePayloadSize = input.size,
                isTruncated = !hasRootEnd,
                message = if (hasRootEnd) "XML đã giải mã hợp lệ" else "XML bị cắt: thiếu </root>",
            )
        }

        if ((input[0].toInt() and 0xFF) != FORMAT_79) return ContainerInspection(
            ContainerKind.UNKNOWN, false, null, (input.size - MIN_HEADER_SIZE).coerceAtLeast(0), false,
            "Sai header: byte đầu không phải 0x79",
        )

        if (input.size < MIN_HEADER_SIZE) return ContainerInspection(
            ContainerKind.BINARY_79, false, null, 0, true,
            "File bị cắt: header cần tối thiểu 8 byte",
        )

        val headerSeed = (input[1].toLong() and 0xFF) or
            ((input[2].toLong() and 0xFF) shl 8) or
            ((input[3].toLong() and 0xFF) shl 16)
        val recovered = ((headerSeed - (810733L xor input.size.toLong())) and 0xFFFF_FFFFL) xor 235176L
        val available = input.size - MIN_HEADER_SIZE
        val declared = if (recovered <= Int.MAX_VALUE) recovered.toInt() else null
        val truncated = declared == null || declared < 0 || declared > available
        return ContainerInspection(
            kind = ContainerKind.BINARY_79,
            validHeader = !truncated,
            declaredPayloadSize = declared,
            availablePayloadSize = available,
            isTruncated = truncated,
            message = if (truncated) {
                "File bị cắt hoặc header sai: payload khai báo ${declared ?: "quá lớn"} byte, thực có $available byte"
            } else {
                "Container 0x79 hợp lệ; payload $declared/$available byte"
            },
        )
    }

    private fun ByteArray.indexOf(needle: ByteArray): Int {
        if (needle.isEmpty() || size < needle.size) return -1
        outer@ for (i in 0..size - needle.size) {
            for (j in needle.indices) if (this[i + j] != needle[j]) continue@outer
            return i
        }
        return -1
    }
}
