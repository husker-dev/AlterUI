package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ResizeAlgorithm
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.c_wideBytes
import java.nio.ByteBuffer
import kotlin.math.max

class WWindow(hwnd: Long): Window(hwnd) {

    companion object {
        @JvmStatic external fun nInitCallbacks(hwnd: Long, callbackObject: Any)
        @JvmStatic external fun nSetVisible(hwnd: Long, visible: Boolean)
        @JvmStatic external fun nSetTitle(hwnd: Long, title: ByteArray)
        @JvmStatic external fun nSetSize(hwnd: Long, x: Int, y: Int, width: Int, height: Int)
        @JvmStatic external fun nRequestRepaint(hwnd: Long)
        @JvmStatic external fun nGetDpi(hwnd: Long): Float
        @JvmStatic external fun nSetIcon(hwnd: Long, width: Int, height: Int, channels: Int, data: ByteBuffer, isBig: Boolean)
        @JvmStatic external fun nSetDefaultIcon(hwnd: Long)

        @JvmStatic external fun nPollEvents()
        @JvmStatic external fun nSendEmptyMessage(handle: Long)
    }

    private val cachedIcons = hashMapOf<Int, Image>()

    init {
        onDpiChangedListeners.add {
            if(icon != null)
                updateIcon()
        }
    }

    private fun updateIcon(){
        val requiredDpi = dpi
        val ratioSmall = (16.0 * dpi) / max(icon!!.width, icon!!.height)
        val ratioBig = (32.0 * dpi) / max(icon!!.width, icon!!.height)

        val smallWidth = (icon!!.width * ratioSmall).toInt()
        val bigWidth = (icon!!.width * ratioBig).toInt()

        if(smallWidth !in cachedIcons)
            cachedIcons[smallWidth] = icon!!.getResized(smallWidth, (icon!!.height * ratioSmall).toInt(), ResizeAlgorithm.Mitchell)
        if(bigWidth !in cachedIcons)
            cachedIcons[bigWidth] = icon!!.getResized(bigWidth, (icon!!.height * ratioBig).toInt(), ResizeAlgorithm.Mitchell)

        val smallIcon = cachedIcons[smallWidth]!!
        val bigIcon = cachedIcons[bigWidth]!!

        if(requiredDpi == dpi) {
            nSetIcon(handle, smallIcon.width, smallIcon.height, smallIcon.pixelType.channels, smallIcon.data, false)
            nSetIcon(handle, bigIcon.width, bigIcon.height, bigIcon.pixelType.channels, bigIcon.data, true)
        }
    }

    override fun initCallbacksImpl() = nInitCallbacks(handle, this)
    override fun getDpiImpl() = nGetDpi(handle)
    override fun setVisibleImpl(visible: Boolean) = nSetVisible(handle, visible)
    override fun setTitleImpl(title: String) = nSetTitle(handle, title.c_wideBytes)
    override fun setSizeImpl(x: Int, y: Int, width: Int, height: Int) = nSetSize(handle, x, y, width, height)
    override fun setIconImpl(image: Image?) {
        if(image != null) updateIcon()
        else nSetDefaultIcon(handle)
    }

    override fun requestRepaint() = nRequestRepaint(handle)

}