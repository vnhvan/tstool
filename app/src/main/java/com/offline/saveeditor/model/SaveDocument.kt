package com.offline.saveeditor.model

data class SaveDocument(
    val container: ByteArray,
    val xml: ByteArray,
    val contentSeed: Long?,
    val containerSize: Int,
    val sha256: String,
    val fields: SaveFields,
    val sourceKind: SaveSourceKind,
) {
    val canEncode: Boolean get() = sourceKind == SaveSourceKind.BINARY_CONTAINER && contentSeed != null
}

enum class SaveSourceKind { BINARY_CONTAINER, DECODED_XML }

data class SaveFields(
    val coin: Long?,
    val tcash: Long?,
    val soundVolume: Int?,
    val mineDepth: Long?,
)
