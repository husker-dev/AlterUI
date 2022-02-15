package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.IntBuffer

class D3D11Pipeline: Pipeline.WindowPoll("d3d11") {

    companion object {
        @JvmStatic external fun nCreateWindow(): Long

        @JvmStatic external fun nGetDevice(hwnd: Long): Long
        @JvmStatic external fun nGetContext(hwnd: Long): Long
        @JvmStatic external fun nGetSwapchain(hwnd: Long): Long

        @JvmStatic external fun nPresent(device: Long)
        @JvmStatic external fun nClear(device: Long, red: Float, green: Float, blue: Float, alpha: Float)
    }

    override fun createWindow(): Window {
        lateinit var window: Window
        MainThreadLocker.invoke {
            window = Platform.current.createWindowInstance(nCreateWindow())
            windows.add(window)
        }
        return window
    }

    override fun createGraphics(window: Window) = D3D11Graphics(window)
    override fun createImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?): Image {
        TODO("Not yet implemented")
    }

    override fun isMainThreadRequired() = true

}