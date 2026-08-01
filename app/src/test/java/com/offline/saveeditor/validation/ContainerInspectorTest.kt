package com.offline.saveeditor.validation

import org.junit.Assert.*
import org.junit.Test

class ContainerInspectorTest {
    @Test fun acceptsDecodedXmlWithLeadingWhitespace() {
        val result = ContainerInspector.inspect(" \n<root><Var name=\"x\" v=\"1\"/></root>junk".toByteArray())
        assertEquals(ContainerKind.XML, result.kind)
        assertTrue(result.validHeader)
        assertFalse(result.isTruncated)
    }

    @Test fun rejectsTruncatedXml() {
        val result = ContainerInspector.inspect("<root>".toByteArray())
        assertEquals(ContainerKind.XML, result.kind)
        assertTrue(result.isTruncated)
    }

    @Test fun rejectsWrongHeader() {
        val result = ContainerInspector.inspect(ByteArray(20) { 0x55 })
        assertEquals(ContainerKind.UNKNOWN, result.kind)
        assertFalse(result.validHeader)
        assertTrue(result.message.contains("Sai header"))
    }

    @Test fun rejectsShort79Header() {
        val result = ContainerInspector.inspect(byteArrayOf(0x79, 1, 2))
        assertEquals(ContainerKind.BINARY_79, result.kind)
        assertTrue(result.isTruncated)
    }
}
