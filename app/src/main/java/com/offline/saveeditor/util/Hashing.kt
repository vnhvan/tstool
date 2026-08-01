package com.offline.saveeditor.util

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

fun sha256(data: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(data)
    .toHex()

fun sha256(input: InputStream, bufferSize: Int = 64 * 1024): String {
    require(bufferSize in 1024..1024 * 1024) { "Kích thước bộ đệm SHA-256 không hợp lệ" }
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(bufferSize)
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        if (read > 0) digest.update(buffer, 0, read)
    }
    return digest.digest().toHex()
}

fun sha256(file: File): String {
    require(file.isFile) { "Không tìm thấy file để tính SHA-256" }
    return file.inputStream().buffered().use(::sha256)
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
