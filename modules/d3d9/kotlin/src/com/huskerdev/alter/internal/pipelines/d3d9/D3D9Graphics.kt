package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.geom.Shape
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.device
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ImagePainter

abstract class D3D9Graphics(open val surface: Long): Graphics() {

    override fun getColorPainter() = D3D9ColorPainter
    override fun getImagePainter() = D3D9ImagePainter

    override fun clear() {
        synchronized(device){
            super.clear()
        }
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(device) {
            super.fillRect(x, y, width, height)
        }
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        synchronized(device) {
            super.drawRect(x, y, width, height)
        }
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        synchronized(device) {
            super.drawImage(image, x, y, width, height)
        }
    }

    override fun drawText(text: String, x: Float, y: Float) {
        synchronized(device) {
            super.drawText(text, x, y)
        }
    }

    override fun fillShape(shape: Shape) {
        synchronized(device) {
            super.fillShape(shape)
        }
    }

    override fun drawShape(shape: Shape) {
        synchronized(device) {
            super.drawShape(shape)
        }
    }

    override fun finish() {}
}

class D3D9ImageGraphics(val image: D3D9Image): D3D9Graphics(image.renderTarget.surface){
    override val width = image.logicWidth.toFloat()
    override val height = image.logicHeight.toFloat()
    override val physicalWidth = image.physicalWidth
    override val physicalHeight = image.physicalHeight
    override val dpi = image.dpi
    override val pixelType = image.pixelType

    override fun clear() {
        super.clear()
        image.renderTarget.contentChanged = true
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        super.fillRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawShape(shape: Shape) {
        super.drawShape(shape)
        image.renderTarget.contentChanged = true
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float) {
        super.drawRect(x, y, width, height)
        image.renderTarget.contentChanged = true
    }

    override fun drawImage(image: Image, x: Float, y: Float, width: Float, height: Float) {
        super.drawImage(image, x, y, width, height)
        this.image.renderTarget.contentChanged = true
    }

    override fun drawText(text: String, x: Float, y: Float) {
        super.drawText(text, x, y)
        image.renderTarget.contentChanged = true
    }

    override fun fillShape(shape: Shape) {
        super.fillShape(shape)
        image.renderTarget.contentChanged = true
    }
}

class D3D9WindowGraphics(val window: WindowPeer): D3D9Graphics(0){
    override val width: Float
        get() = window.width
    override val height: Float
        get() = window.height
    override val physicalHeight: Int
        get() = window.clientHeight
    override val physicalWidth: Int
        get() = window.clientWidth
    override val dpi: Float
        get() = window.dpi
    override val pixelType = PixelType.RGBA

    override val surface: Long
        get() = device.getWindowSurface(window.handle)

    override fun finish() = device.present(window.handle)
}