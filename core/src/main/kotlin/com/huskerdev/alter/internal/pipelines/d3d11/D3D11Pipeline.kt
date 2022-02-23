package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class D3D11Pipeline: Pipeline.WindowPoll("d3d11") {

    companion object {
        @JvmStatic external fun nCreateContext()
        @JvmStatic external fun nCreateWindow(): Long

        @JvmStatic external fun setRenderTarget(hwnd: Long)
        @JvmStatic external fun setViewport(width: Int, height: Int)
        @JvmStatic external fun nPresent(hwnd: Long)
        @JvmStatic external fun nClear(hwnd: Long)

        @JvmStatic external fun nCreatePixelShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nCreateVertexShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nSetPixelShader(pointer: Long)
        @JvmStatic external fun nSetVertexShader(pointer: Long)
        @JvmStatic external fun nSetShaderValue4f(pointer: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float, v4: Float)
        @JvmStatic external fun nSetShaderValue3f(pointer: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float)
        @JvmStatic external fun nSetShaderValue1f(pointer: Long, name: ByteBuffer, v1: Float)
        @JvmStatic external fun nSetShaderMatrix(pointer: Long, name: ByteBuffer, matrix: FloatBuffer)
    }

    override fun load() {
        super.load()
        MainThreadLocker.invoke {
            nCreateContext()
        }
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