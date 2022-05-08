package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.painters.VertexHelper
import com.huskerdev.alter.internal.utils.kotlin.unique
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class D3D9Device {

    companion object {

        @JvmStatic private external fun nGetWindowSurface(hwnd: Long): Long
        @JvmStatic private external fun nGetTextureSurface(texture: Long): Long
        @JvmStatic private external fun nReleaseTexture(texture: Long)
        @JvmStatic private external fun nReleaseSurface(surface: Long)

        @JvmStatic private external fun nSetRenderTarget(surface: Long)
        @JvmStatic private external fun nBeginScene()
        @JvmStatic private external fun nEndScene()
        @JvmStatic private external fun nPresent(hwnd: Long)
        @JvmStatic private external fun nClear()
        @JvmStatic private external fun nSetTexture(index: Int, texture: Long)

        @JvmStatic private external fun nDrawArrays(array: FloatBuffer, points: Int, type: Int)
        @JvmStatic private external fun nCreatePixelShader(content: ByteBuffer, length: Int): Long
        @JvmStatic private external fun nCreateVertexShader(content: ByteBuffer, length: Int): Long
        @JvmStatic private external fun nSetPixelShader(shader: Long)
        @JvmStatic private external fun nSetVertexShader(shader: Long)
        @JvmStatic private external fun nGetShaderVariableHandle(shader: Long, name: ByteBuffer): Long
        @JvmStatic private external fun nSetShaderValue1f(shader: Long, varHandle: Long, v: Float)
        @JvmStatic private external fun nSetShaderValue3f(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float)
        @JvmStatic private external fun nSetShaderValue4f(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float, v4: Float)
        //@JvmStatic external fun nSetShaderMatrix(shader: Long, varHandle: Long, matrix: FloatBuffer)
        @JvmStatic private external fun nCreateEmptySurface(width: Int, height: Int, components: Int, samples: Int): Long
        @JvmStatic private external fun nCreateSurface(width: Int, height: Int, components: Int, samples: Int, data: ByteBuffer): Long
        @JvmStatic private external fun nCreateTexture(width: Int, height: Int, components: Int): Long
        @JvmStatic private external fun nStretchRect(surfaceFrom: Long, surfaceTo: Long)
        @JvmStatic private external fun nSetLinearFiltering(linearFiltering: Boolean)
        @JvmStatic private external fun nGetSurfaceData(surface: Long, x: Int, y: Int, width: Int, height: Int, components: Int): ByteBuffer
    }

    var pixelShader by unique<D3D9Shader?>(null){
        if(it != null)
            nSetPixelShader(it.pointer)
    }

    var vertexShader by unique<D3D9Shader?>(null){
        if(it != null)
            nSetVertexShader(it.pointer)
    }

    var surface by unique(0L){
        nSetRenderTarget(it)
    }

    var linearFiltering by unique(true){
        nSetLinearFiltering(it)
    }

    fun getWindowSurface(hwnd: Long) = nGetWindowSurface(hwnd)

    fun getTextureSurface(texture: Long) = nGetTextureSurface(texture)

    fun releaseTexture(texture: Long) = nReleaseTexture(texture)
    fun releaseSurface(surface: Long) = nReleaseSurface(surface)

    fun beginScene() = nBeginScene()
    fun endScene() = nEndScene()

    fun present(hwnd: Long) = nPresent(hwnd)

    fun createPixelShader(content: ByteBuffer, length: Int) = nCreatePixelShader(content, length)
    fun createVertexShader(content: ByteBuffer, length: Int) = nCreateVertexShader(content, length)

    fun getShaderVariableHandle(shader: Long, name: ByteBuffer) = nGetShaderVariableHandle(shader, name)
    fun setShaderValue(shader: Long, varHandle: Long, v: Float) = nSetShaderValue1f(shader, varHandle, v)
    fun setShaderValue(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float) = nSetShaderValue3f(shader, varHandle, v1, v2, v3)
    fun setShaderValue(shader: Long, varHandle: Long, v1: Float, v2: Float, v3: Float, v4: Float) = nSetShaderValue4f(shader, varHandle, v1, v2, v3, v4)

    fun createEmptySurface(width: Int, height: Int, components: Int, samples: Int) = nCreateEmptySurface(width, height, components, samples)
    fun createSurface(width: Int, height: Int, components: Int, samples: Int, data: ByteBuffer) = nCreateSurface(width, height, components, samples, data)
    fun createTexture(width: Int, height: Int, components: Int) = nCreateTexture(width, height, components)
    fun stretchRect(surfaceFrom: Long, surfaceTo: Long) = nStretchRect(surfaceFrom, surfaceTo)
    fun getSurfaceData(surface: Long, x: Int, y: Int, width: Int, height: Int, components: Int) = nGetSurfaceData(surface, x, y, width, height, components)

    fun clear() = nClear()

    fun drawVertices(buffer: FloatBuffer, count: Int, type: VertexHelper.DrawType) =
        nDrawArrays(buffer, count, type.ordinal + 1)

    fun bindTexture(index: Int, texture: Long) =
        nSetTexture(index, texture)
}