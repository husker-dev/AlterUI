package com.huskerdev.alter.internal.pipelines.d3d9

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.Window
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nBeginScene
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nClear
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nEndScene
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nGetDevice
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline.Companion.nSetViewport
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3D9ColorPainter
import com.huskerdev.alter.internal.pipelines.d3d9.painters.D3DPainter

class D3D9Graphics(window: Window): Graphics(window) {

    companion object {
        var currentDevice = 0L
    }

    private val colorPainterInstance by lazy { D3D9ColorPainter() }

    private val device = nGetDevice(window.handle)
    private var oldWidth = -1
    private var oldHeight = -1

    override fun beginImpl() {
        currentDevice = device
        if(oldWidth != window.width.toInt() || oldHeight != window.height.toInt()) {
            oldWidth = window.width.toInt()
            oldHeight = window.height.toInt()
            nSetViewport(device, oldWidth, oldHeight)
        }
        nBeginScene(device)
    }

    override fun endImpl() {
        nEndScene(device)
    }

    override fun updateMatrix() {
        if(painter is D3DPainter)
            (painter as D3DPainter).updateMatrix(matrix)
    }

    override fun clear() = nClear(device)

    override fun getColorPainter() = colorPainterInstance
}