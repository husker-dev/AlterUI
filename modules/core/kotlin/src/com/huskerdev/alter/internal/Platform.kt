package com.huskerdev.alter.internal

import com.huskerdev.alter.Monitor
import com.huskerdev.alter.OS
import com.huskerdev.alter.geom.Point
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
val ByteArray.c_wstr: ByteArray
    get() {
        val cBytes = ByteArray(this.size + 2)
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
            try {
                val platformObject = Class.forName("com.huskerdev.alter.internal.platforms.${OS.current.shortName}.${OS.current.name}Platform")
                return@lazy platformObject.constructors[0].newInstance() as Platform
            }catch (e: Exception){
                throw UnsupportedOperationException("Unsupported OS - ${OS.current.name} (${OS.current.shortName})")
            }
        }

        fun initialize(){
            current.load()
        }
    }

    abstract val defaultFontFamily: String
    abstract val mousePosition: Point
    abstract val physicalMousePosition: Point

    abstract fun load()
    abstract fun createWindowInstance(handle: Long): WindowPeer
    abstract fun pollEvents()
    abstract fun takeEvents()
    abstract fun sendEmptyMessage(handle: Long)
    abstract fun getFontData(name: String): ByteBuffer?
    abstract fun getPrimaryMonitor(): Monitor
    abstract fun getMonitors(): Array<Monitor>
}