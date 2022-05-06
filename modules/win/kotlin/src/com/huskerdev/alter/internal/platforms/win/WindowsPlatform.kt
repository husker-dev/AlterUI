package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.c_wideBytes
import com.huskerdev.alter.internal.utils.BufferUtils
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.LibraryLoader
import com.huskerdev.alter.internal.utils.MainThreadLocker
import com.huskerdev.alter.os.MessageBox
import com.huskerdev.alter.os.MessageBoxButton
import java.nio.ByteBuffer

@ImplicitUsage
class WindowsPlatform: Platform() {

    companion object {
        @JvmStatic external fun nGetFontData(family: ByteBuffer): ByteBuffer?
        @JvmStatic external fun nGetMouseX(): Int
        @JvmStatic external fun nGetMouseY(): Int
        @JvmStatic external fun nGetMouseDpi(): Float
        @JvmStatic external fun nShowMessage(hwnd: Long, title: ByteBuffer, content: ByteBuffer, icon: Int, type: Int): Int
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

    override fun getPrimaryMonitor() = WMonitorPeer.primary
    override fun getMonitors() = WMonitorPeer.list

    override fun showMessage(context: WindowPeer?, message: MessageBox): MessageBoxButton {
        var result = 0
        MainThreadLocker.invoke {
            result = nShowMessage(
                context?.handle ?: 0,
                BufferUtils.createByteBuffer(*message.title.c_wideBytes),
                BufferUtils.createByteBuffer(*message.message.c_wideBytes),
                message.icon.ordinal,
                message.type.ordinal
            )
        }
        return when(result){
            1 -> MessageBoxButton.No
            2 -> MessageBoxButton.Cancel
            3 -> MessageBoxButton.Abort
            4 -> MessageBoxButton.Retry
            5 -> MessageBoxButton.Ignore
            6 -> MessageBoxButton.Yes
            7 -> MessageBoxButton.No
            10 -> MessageBoxButton.TryAgain
            11 -> MessageBoxButton.Continue
            else -> throw UnsupportedOperationException("Unsupported button")
        }
    }

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