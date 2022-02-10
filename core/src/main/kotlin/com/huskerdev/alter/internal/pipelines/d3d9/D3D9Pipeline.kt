package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class D3D9Pipeline: Pipeline.WindowPoll("d3d9") {

    companion object {
        @JvmStatic external fun nCreateWindow(): Long
        @JvmStatic external fun nGetDevice(hwnd: Long): Long

        @JvmStatic external fun nSetViewport(device: Long, width: Int, height: Int)
        @JvmStatic external fun nBeginScene(device: Long)
        @JvmStatic external fun nEndScene(device: Long)
        @JvmStatic external fun nClear(device: Long)
        @JvmStatic external fun nDrawArrays(device: Long, array: FloatBuffer, points: Int)
        @JvmStatic external fun nCreatePixelShader(device: Long, content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nCreateVertexShader(device: Long, content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nSetPixelShader(device: Long, shader: Long)
        @JvmStatic external fun nSetVertexShader(device: Long, shader: Long)
        @JvmStatic external fun nSetShaderValue3f(device: Long, shader: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float)
        @JvmStatic external fun nSetShaderValue4f(device: Long, shader: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float, v4: Float)
        @JvmStatic external fun nSetShaderMatrix(device: Long, shader: Long, name: ByteBuffer, matrix: FloatBuffer)
    }

    override fun createWindow(): Window {
        lateinit var window: Window
        MainThreadLocker.invoke {
            window = Platform.current.createWindowInstance(nCreateWindow())
            windows.add(window)
        }
        return window
    }

    override fun createGraphics(window: Window) = D3D9Graphics(window)

    override fun isUIRequireMainThread() = true

}