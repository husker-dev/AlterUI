package com.huskerdev.alter.internal

import com.huskerdev.alter.OS
import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.internal.platforms.win.WindowsPlatform
import com.huskerdev.alter.internal.platforms.win.c_wstr
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

val String.c_str: ByteArray
    get() = encodeToByteArray().c_str

val ByteArray.c_str: ByteArray
    get() {
        val cBytes = ByteArray(this.size + 1)
        System.arraycopy(this, 0, cBytes, 0, this.size)
        return cBytes
    }

// For windows platform
val String.wideBytes: ByteArray
    get() = encodeToByteArray().wideBytes
val String.c_wideBytes: ByteArray
    get() = toByteArray(StandardCharsets.UTF_16LE).c_wstr
val ByteArray.wideBytes: ByteArray
    get() = String(this, StandardCharsets.UTF_8).toByteArray(StandardCharsets.UTF_16LE)
val ByteArray.utf8BytesFromWide: ByteArray
    get() = String(this, StandardCharsets.UTF_16LE).toByteArray(StandardCharsets.UTF_8)
val ByteArray.utf8TextFromWide: String
    get() = String(this, StandardCharsets.UTF_16LE)

abstract class Platform {

    companion object {

        val current by lazy {
            when(OS.current){
                OS.Windows -> WindowsPlatform()
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
        }

        fun initialize(){
            current.load()
        }
    }

    abstract val defaultFontFamily: String
    abstract val mousePosition: Point<Float>
    abstract val physicalMousePosition: Point<Int>

    abstract fun load()
    abstract fun createWindowInstance(handle: Long): WindowPeer
    abstract fun pollEvents()
    abstract fun sendEmptyMessage(handle: Long)
    abstract fun getFontData(name: String): ByteBuffer?
}