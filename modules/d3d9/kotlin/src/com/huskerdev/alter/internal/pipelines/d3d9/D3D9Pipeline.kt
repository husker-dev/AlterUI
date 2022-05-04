package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.PixelType
import com.huskerdev.alter.graphics.filters.GaussianBlur
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.Platform
import com.huskerdev.alter.internal.WindowPeer
import com.huskerdev.alter.internal.pipelines.d3d9.filters.D3D9GaussianBlur
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.nio.ByteBuffer
import java.nio.FloatBuffer

@ImplicitUsage
class D3D9Pipeline: Pipeline.DefaultEventPoll("d3d9") {

    companion object {

        val device = D3D9Device()

        @JvmStatic external fun nInitializeDevice(vsync: Boolean, samples: Int)
        @JvmStatic external fun nCreateWindow(): Long
    }

    override fun load() {
        super.load()
        nInitializeDevice(AlterUIProperties.vsync, AlterUIProperties.msaa)
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
    ) = D3D9Image(AlterUIProperties.msaa, width, height, width, height, type, 1f, data)

    override fun createSurfaceImage(
        window: WindowPeer,
        type: PixelType,
        physicalWidth: Int,
        physicalHeight: Int,
        logicWidth: Int,
        logicHeight: Int,
        dpi: Float
    ) = D3D9Image(AlterUIProperties.msaa, physicalWidth, physicalHeight, logicWidth, logicHeight, type, dpi, null)

    override fun isMainThreadRequired() = true

    // Image filters
    override fun createGaussianBlurFilter(radius: Int) = D3D9GaussianBlur(radius)

}