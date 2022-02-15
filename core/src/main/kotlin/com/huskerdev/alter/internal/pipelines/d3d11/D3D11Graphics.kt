package com.huskerdev.alter.internal.pipelines.d3d11

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.painters.ColorPainter
import com.huskerdev.alter.graphics.painters.ImagePainter
import com.huskerdev.alter.internal.Window

class D3D11Graphics(window: Window): Graphics(window) {

    private val device = D3D11Pipeline.nGetDevice(window.handle)
    private val context = D3D11Pipeline.nGetContext(window.handle)
    private val swapchain = D3D11Pipeline.nGetSwapchain(window.handle)

    override fun beginImpl() {
        D3D11Pipeline.nPresent(device)
    }

    override fun endImpl() {

    }

    override fun updateMatrix() {

    }

    override fun clear() {

    }


    override fun getColorPainter(): ColorPainter {
        TODO("Not yet implemented")
    }

    override fun getImagePainter(): ImagePainter {
        TODO("Not yet implemented")
    }
}