package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.FloatBuffer

@ImplicitUsage
class D3D9Pipeline: Pipeline.DefaultEventPoll("d3d9") {

    companion object {

        val device = D3D9Device()

        @JvmStatic external fun nInitializeDevice()
        @JvmStatic external fun nCreateWindow(): Long
        @JvmStatic external fun nGetWindowSurface(hwnd: Long): Long
        @JvmStatic external fun nGetTextureSurface(texture: Long): Long
        @JvmStatic external fun nReleaseTexture(texture: Long)
        @JvmStatic external fun nReleaseSurface(surface: Long)

        @JvmStatic external fun nSetRenderTarget(surface: Long)
        @JvmStatic external fun nBeginScene()
        @JvmStatic external fun nEndScene()
        @JvmStatic external fun nPresent(hwnd: Long)
        @JvmStatic external fun nClear()
        @JvmStatic external fun nSetTexture(index: Int, texture: Long)

        @JvmStatic external fun nDrawArrays(array: FloatBuffer, points: Int, type: Int)
        @JvmStatic external fun nCreatePixelShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nCreateVertexShader(content: ByteBuffer, length: Int): Long
        @JvmStatic external fun nSetPixelShader(shader: Long)
        @JvmStatic external fun nSetVertexShader(shader: Long)
        @JvmStatic external fun nGetShaderVariableHandle(shader: Long, name: ByteBuffer): Long
        @JvmStatic external fun nSetShaderValue1f(shader: Long, varHandle: Long, v: Float)
        @JvmStatic external fun nSetShaderValue3f(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float)
        @JvmStatic external fun nSetShaderValue4f(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float, v4: Float)
        @JvmStatic external fun nSetShaderMatrix(shader: Long, varHandle: Long, matrix: FloatBuffer)
        @JvmStatic external fun nCreateTexture(width: Int, height: Int, components: Int, data: ByteBuffer): Long
        @JvmStatic external fun nCreateEmptyTexture(width: Int, height: Int, components: Int): Long
        @JvmStatic external fun nSetLinearFiltering(linearFiltering: Boolean)
    }

    override fun load() {
        super.load()
        nInitializeDevice()
    }

    override fun createWindow(): WindowPeer {
        lateinit var window: WindowPeer
        MainThreadLocker.invoke {
            window = Platform.current.createWindowInstance(nCreateWindow())
            windows.add(window)
        }
        return window
    }

    override fun createGraphics(window: WindowPeer) = D3D9WindowGraphics(window)
    override fun createGraphics(image: Image) = D3D9ImageGraphics(image as D3D9Image)

    override fun createImage(
        type: PixelType,
        width: Int,
        height: Int,
        data: ByteBuffer?
    ): Image {
        val texture = if(data != null)
            nCreateTexture(width, height, type.channels, data)
        else
            nCreateEmptyTexture(width, height, type.channels)
        val surface = nGetTextureSurface(texture)

        return D3D9Image(texture, surface, width, height, width, height, type, 1f)
    }

    override fun createSurfaceImage(
        window: WindowPeer,
        type: PixelType,
        physicalWidth: Int,
        physicalHeight: Int,
        logicWidth: Int,
        logicHeight: Int,
        dpi: Float
    ): Image {
        val texture = nCreateEmptyTexture(physicalWidth, physicalHeight, type.channels)
        val surface = nGetTextureSurface(texture)

        return D3D9Image(texture, surface, physicalWidth, physicalHeight, logicWidth, logicHeight, type, dpi)
    }

    override fun isMainThreadRequired() = true

}