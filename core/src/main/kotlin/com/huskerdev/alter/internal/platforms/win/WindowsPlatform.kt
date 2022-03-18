package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.c_wideBytes
import com.huskerdev.alter.internal.utils.BufferUtils
import com.huskerdev.alter.internal.utils.LibraryLoader
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets




val ByteArray.c_wstr: ByteArray
    get() {
        val cBytes = ByteArray(this.size + 2)
        System.arraycopy(this, 0, cBytes, 0, this.size)
        return cBytes
    }

class WindowsPlatform: Platform() {

    companion object {
        @JvmStatic external fun nGetFontData(family: ByteBuffer): ByteBuffer?
    }

    override val defaultFontFamily = "Arial"

    override fun load() {
        LibraryLoader.loadModuleLib("win")
    }

    override fun createWindowInstance(handle: Long) = WWindow(handle)

    override fun pollEvents() = WWindow.nPollEvents()
    override fun sendEmptyMessage(handle: Long) = WWindow.nSendEmptyMessage(handle)
    override fun getFontData(name: String) = nGetFontData(BufferUtils.createByteBuffer(*name.c_wideBytes))

}