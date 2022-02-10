package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.internal.Window

class WWindow(hwnd: Long): Window(hwnd) {

    companion object {
        @JvmStatic external fun nInitCallbacks(hwnd: Long, callbackObject: Any)
        @JvmStatic external fun nSetVisible(hwnd: Long, visible: Boolean)
        @JvmStatic external fun nSetTitle(hwnd: Long, title: ByteArray)
        @JvmStatic external fun nSetSize(hwnd: Long, x: Int, y: Int, width: Int, height: Int)
        @JvmStatic external fun nRequestRepaint(hwnd: Long)

        @JvmStatic external fun nPollEvents()
        @JvmStatic external fun nSendEmptyMessage(handle: Long)
    }

    override fun initCallbacks() = nInitCallbacks(handle, this)
    override fun setVisibleImpl(visible: Boolean) = nSetVisible(handle, visible)
    override fun setTitleImpl(title: String) = nSetTitle(handle, title.c_wideBytes)
    override fun setSizeImpl(x: Double, y: Double, width: Double, height: Double) = nSetSize(handle, x.toInt(), y.toInt(), width.toInt(), height.toInt())

    override fun requestRepaint() = nRequestRepaint(handle)


}