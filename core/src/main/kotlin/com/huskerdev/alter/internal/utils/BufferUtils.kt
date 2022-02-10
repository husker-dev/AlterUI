package com.huskerdev.alter.internal.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

class BufferUtils {
    companion object {
        fun createByteBuffer(capacity: Int) = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
        fun createFloatBuffer(capacity: Int) = createByteBuffer(capacity * Float.SIZE_BYTES).asFloatBuffer()
        fun createIntBuffer(capacity: Int) = createByteBuffer(capacity * Int.SIZE_BYTES).asIntBuffer()

        fun createByteBuffer(vararg elements: Byte) = createByteBuffer(elements.size).put(elements)!!
        fun createFloatBuffer(vararg elements: Float) = createByteBuffer(elements.size * Float.SIZE_BYTES).asFloatBuffer().put(elements)!!
        fun createIntBuffer(vararg elements: Int) = createByteBuffer(elements.size * Int.SIZE_BYTES).asIntBuffer().put(elements)!!
    }
}