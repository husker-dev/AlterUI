package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.ImageType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class D3D9Pipeline: Pipeline.WindowPoll("d3d9") {

    companion object {
        var mainWindow = 0L
        var resourceThread: Thread? = null

        @JvmStatic external fun nCreateMainWindow(): Long
        @JvmStatic external fun nCreateWindow(): Long
        @JvmStatic external fun nGetDevice(): Long

        @JvmStatic external fun nBeginScene(hwnd: Long)
        @JvmStatic external fun nEndScene(hwnd: Long)
        @JvmStatic external fun nClear()
        @JvmStatic external fun nSetTexture(texture: Long)

        @JvmStatic external fun nSetViewport(device: Long, width: Int, height: Int)
        @JvmStatic external fun nDrawArrays(array: FloatBuffer, points: Int, type: Int)
        @JvmStatic external fun nCreatePixelShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nCreateVertexShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nSetPixelShader(shader: Long)
        @JvmStatic external fun nSetVertexShader(shader: Long)
        @JvmStatic external fun nSetShaderValue1f(shader: Long, name: ByteBuffer, v: Float)
        @JvmStatic external fun nSetShaderValue3f(shader: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float)
        @JvmStatic external fun nSetShaderValue4f(shader: Long, name: ByteBuffer, v1: Float, v2: Float, v3: Float, v4: Float)
        @JvmStatic external fun nSetShaderMatrix(shader: Long, name: ByteBuffer, matrix: FloatBuffer)
        @JvmStatic external fun nCreateTexture(width: Int, height: Int, components: Int, data: ByteBuffer): Long
        @JvmStatic external fun nSetLinearFiltering(linearFiltering: Boolean)
    }


    override fun load() {
        super.load()

        MainThreadLocker.invoke {
            mainWindow = nCreateMainWindow()
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

    override fun createGraphics(window: Window) = D3D9Graphics(window)
    override fun createImage(type: ImageType, width: Int, height: Int, data: ByteBuffer?) = D3D9Image(width, height, type, data)

    override fun isMainThreadRequired() = true

}