package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.c_wideBytes
import com.huskerdev.alter.internal.utils.BufferUtils
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.LibraryLoader
import java.nio.ByteBuffer

@ImplicitUsage
class WindowsPlatform: Platform() {

    companion object {
        @JvmStatic external fun nGetFontData(family: ByteBuffer): ByteBuffer?
        @JvmStatic external fun nGetMouseX(): Int
        @JvmStatic external fun nGetMouseY(): Int
        @JvmStatic external fun nGetMouseDpi(): Float
    }

    override val defaultFontFamily = "Arial"

    override fun load() {
        LibraryLoader.loadModuleLib("win")
    }

    override fun createWindowInstance(handle: Long) = WWindowPeer(handle)

    override fun pollEvents() = WWindowPeer.nPollEvents()
    override fun takeEvents() = WWindowPeer.nTakeEvents()

    override fun sendEmptyMessage(handle: Long) = WWindowPeer.nSendEmptyMessage(handle)
    override fun getFontData(name: String) = nGetFontData(BufferUtils.createByteBuffer(*name.c_wideBytes))

    override val mousePosition: Point
        get() {
            val dpi = nGetMouseDpi()
            val x = nGetMouseX().toFloat() / dpi
            val y = nGetMouseY().toFloat() / dpi
            return Point(x, y)
        }
    override val physicalMousePosition: Point
        get() = Point(nGetMouseX().toFloat(), nGetMouseY().toFloat())
}