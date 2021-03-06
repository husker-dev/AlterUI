package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ResizeAlgorithm
import com.huskerdev.alter.internal.*
import com.huskerdev.alter.internal.utils.BufferUtils
import java.nio.ByteBuffer
import kotlin.math.max

class WWindowPeer(hwnd: Long): WindowPeer(hwnd) {

    companion object {
        @JvmStatic external fun nInitCallbacks(hwnd: Long, callbackObject: Any)
        @JvmStatic external fun nInit(hwnd: Long)
        @JvmStatic external fun nSetVisible(hwnd: Long, visible: Boolean)
        @JvmStatic external fun nSetTitle(hwnd: Long, title: ByteBuffer)
        @JvmStatic external fun nSetSize(hwnd: Long, x: Int, y: Int, width: Int, height: Int)
        @JvmStatic external fun nRequestRepaint(hwnd: Long)
        @JvmStatic external fun nGetDpi(hwnd: Long): Float
        @JvmStatic external fun nSetIcon(hwnd: Long, width: Int, height: Int, channels: Int, data: ByteBuffer, isBig: Boolean)
        @JvmStatic external fun nSetDefaultIcon(hwnd: Long)
        @JvmStatic external fun nSetIconState(hwnd: Long, type: Int)
        @JvmStatic external fun nSetIconProgress(hwnd: Long, progress: Float)
        @JvmStatic external fun nSetStyle(hwnd: Long, style: Int)
        @JvmStatic external fun nSetWindowTitleColor(hwnd: Long, color: Int)
        @JvmStatic external fun nSetWindowTextColor(hwnd: Long, color: Int)
        @JvmStatic external fun nGetWindowMouseX(hwnd: Long): Int
        @JvmStatic external fun nGetWindowMouseY(hwnd: Long): Int

        @JvmStatic external fun nPollEvents()
        @JvmStatic external fun nTakeEvents()
        @JvmStatic external fun nSendEmptyMessage(handle: Long)
    }

    private val cachedIcons = hashMapOf<Int, Image>()
    private var isInitialised = false

    override var visible: Boolean
        get() = super.visible
        set(value) {
            super.visible = value
            if(!isInitialised) {
                isInitialised = true
                nInit(handle)
            }

            if(value) {
                // Update properties when window became visible
                status = status
                progress = progress
            }
        }

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
    override fun setTitleImpl(title: String) = nSetTitle(handle, BufferUtils.createByteBuffer(*title.c_wideBytes))
    override fun setSizeImpl(x: Int, y: Int, width: Int, height: Int) = nSetSize(handle, x, y, width, height)
    override fun setIconImpl(image: Image?) {
        cachedIcons.clear()
        if(image != null) updateIcon()
        else nSetDefaultIcon(handle)
    }

    override fun setStatusImpl(status: WindowStatus) {
        if(!visible)
            return
        nSetIconState(handle, status.ordinal)
    }

    override fun setProgressImpl(progress: Float) {
        if(!visible)
            return
        if(progress == -1f)
            nSetIconState(handle, -1)
        else
            nSetIconProgress(handle, progress)
    }

    override fun setStyleImpl(style: WindowStyle) = nSetStyle(handle, style.styleIndex)
    override fun setColorImpl(color: Color?) = nSetWindowTitleColor(handle, color?.toBGR() ?: -1)
    override fun setTextColorImpl(color: Color?) = nSetWindowTextColor(handle, color?.toBGR() ?: -1)
    override fun getMouseXImpl() = nGetWindowMouseX(handle)
    override fun getMouseYImpl() = nGetWindowMouseY(handle)

    override fun requestRepaint() = nRequestRepaint(handle)

}