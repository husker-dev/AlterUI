package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.d3d11.painters.D3D11ColorPainter

class D3D11Graphics(window: Window): Graphics(window) {

    private val colorPainterInstance = D3D11ColorPainter()

    override fun beginImpl() {
        //D3D11Pipeline.setRenderTarget(window.handle)
        //D3D11Pipeline.setViewport(window.width.toInt(), window.height.toInt())
    }

    override fun endImpl() {
        D3D11Pipeline.nPresent(window.handle)
    }

    override fun updateMatrix() {

    }

    override fun clear() {
        D3D11Pipeline.nClear(window.handle)
    }


    override fun getColorPainter() = colorPainterInstance

    override fun getImagePainter(): ImagePainter {
        TODO("Not yet implemented")
    }
}