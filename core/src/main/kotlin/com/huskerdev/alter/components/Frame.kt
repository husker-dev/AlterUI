package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.Pipeline

open class Frame {

    val peer = Pipeline.current.createWindow()

    var x by peer::x
    var y by peer::y
    var width by peer::width
    var height by peer::height
    val physicalWidth by peer::physicalWidth
    val physicalHeight by peer::physicalHeight
    val clientWidth by peer::clientWidth
    val clientHeight by peer::clientHeight

    var title by peer::title
    var background by peer::background
    var visible by peer::visible
    var icon by peer::icon

    fun onClosed(listener: () -> Unit) = peer.onClosedListeners.add(listener)
    fun onResized(listener: () -> Unit) = peer.onResizedListeners.add(listener)
    fun onMoved(listener: () -> Unit) = peer.onMovedListeners.add(listener)

    init {
        peer.onPaintEvent = this::paint
    }

    fun repaint() = peer.repaint()

    open fun paint(gr: Graphics){
        gr.clear()
        gr.color = background
        gr.fillRect(0f, 0f, width, height)
    }

}