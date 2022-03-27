package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nGetWindowSurface
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nPresent
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ImagePainter

abstract class D3D9Graphics(open val surface: Long): Graphics() {

    override fun getColorPainter() = D3D9ColorPainter
    override fun getImagePainter() = D3D9ImagePainter

    override fun clear() {
        synchronized(D3D9Pipeline.device){
            super.clear()
        }
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(D3D9Pipeline.device) {
            super.fillRect(x, y, width, height)
        }
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(D3D9Pipeline.device) {
            super.drawRect(x, y, width, height)
        }
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        synchronized(D3D9Pipeline.device) {
            super.drawImage(image, x, y, width, height)
        }
    }

    override fun drawText(text: String, x: Float, y: Float) {
        synchronized(D3D9Pipeline.device) {
            super.drawText(text, x, y)
        }
    }

    override fun finish() {}
}

class D3D9ImageGraphics(val image: Image): D3D9Graphics((image as D3D9Image).surface){
    override val width = image.width.toFloat()
    override val height = image.height.toFloat()
    override val physicalHeight = image.height
    override val physicalWidth = image.width
    override val dpi = 1f
    override val pixelType = image.pixelType
}

class D3D9WindowGraphics(val window: WindowPeer): D3D9Graphics(0){
    override val width: Float
        get() = window.width
    override val height: Float
        get() = window.height
    override val physicalHeight: Int
        get() = window.physicalHeight
    override val physicalWidth: Int
        get() = window.physicalWidth
    override val dpi: Float
        get() = window.dpi
    override val pixelType = PixelType.RGBA

    override val surface: Long
        get() = nGetWindowSurface(window.handle)

    override fun finish() = nPresent(window.handle)
}