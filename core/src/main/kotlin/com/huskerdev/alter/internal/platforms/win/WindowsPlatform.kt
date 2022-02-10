package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.internal.Platform
import java.nio.charset.StandardCharsets

val String.wideBytes: ByteArray
    get() = encodeToByteArray().wideBytes
val String.c_wideBytes: ByteArray
    get() = encodeToByteArray().wideBytes.c_wstr
val ByteArray.wideBytes: ByteArray
    get() = String(this, StandardCharsets.UTF_8).toByteArray(StandardCharsets.UTF_16LE)
val ByteArray.utf8Bytes: ByteArray
    get() = String(this, StandardCharsets.UTF_16LE).toByteArray(StandardCharsets.UTF_8)
val ByteArray.utf8Text: String
    get() = String(this, StandardCharsets.UTF_16LE)

val ByteArray.c_wstr: ByteArray
    get() {
        val cBytes = ByteArray(this.size + 2)
        System.arraycopy(this, 0, cBytes, 0, this.size)
        return cBytes
    }


class WindowsPlatform: Platform() {

    override fun load() {
        loadDefaultLibrary()
    }

    override fun createWindowInstance(handle: Long) = WWindow(handle)

    override fun pollEvents() = WWindow.nPollEvents()
    override fun sendEmptyMessage(handle: Long) = WWindow.nSendEmptyMessage(handle)
}