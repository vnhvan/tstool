package com.offline.saveeditor.io

import java.io.ByteArrayOutputStream
import java.io.InputStream

object LimitedInput {
    const val DEFAULT_MAX_BYTES: Int = 64 * 1024 * 1024

    fun read(input: InputStream, maxBytes: Int = DEFAULT_MAX_BYTES, bufferSize: Int = 32 * 1024): ByteArray {
        require(maxBytes > 0) { "maxBytes phải lớn hơn 0" }
        require(bufferSize in 1024..(1024 * 1024)) { "bufferSize không hợp lệ" }
        val initial = minOf(maxBytes, 256 * 1024)
        val output = ByteArrayOutputStream(initial)
        val buffer = ByteArray(bufferSize)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            total += count
            require(total <= maxBytes) { "File vượt giới hạn ${maxBytes / (1024 * 1024)} MB" }
            output.write(buffer, 0, count)
        }
        require(total > 0) { "File rỗng" }
        return output.toByteArray()
    }
}
