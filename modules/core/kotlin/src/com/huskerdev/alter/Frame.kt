package com.huskerdev.alter

import com.huskerdev.alter.components.Component
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.Pipeline

open class Frame {

    val peer = Pipeline.current.createWindow()

    var content: Component? = null
        set(value) {
            if(field != value){
                field = value
                field?.width = width
                field?.height = height
                repaint()
            }
        }

    var x by peer::x
    var y by peer::y
    val physicalX by peer::physicalX
    val physicalY by peer::physicalY
    var width by peer::width
    var height by peer::height
    val physicalWidth by peer::physicalWidth
    val physicalHeight by peer::physicalHeight
    val clientWidth by peer::clientWidth
    val clientHeight by peer::clientHeight
    val dpi by peer::dpi

    var title by peer::title
    var background by peer::background
    var visible by peer::visible
    var icon by peer::icon
    var status by peer::status
    var progress by peer::progress
    var style by peer::style
    var titleColor by peer::color
    var textColor by peer::textColor

    val mousePosition by peer::mousePosition

    fun onClosed(listener: () -> Unit) = peer.onClosedListeners.add(listener)
    fun onResized(listener: () -> Unit) = peer.onResizedListeners.add(listener)
    fun onMoved(listener: () -> Unit) = peer.onMovedListeners.add(listener)

    fun onMouseMoved(listener: () -> Unit) = peer.onMouseMovedListeners.add(listener)

    init {
        peer.onPaintEvent = this::paint
        peer.onResizedListeners.add {
            content?.width = width
            content?.height = height
        }
    }

    fun repaint() = peer.repaint()

    open fun paint(gr: Graphics){
        gr.clear()
        gr.color = background
        gr.fillRect(0f, 0f, width, height)

        content?.paint(gr)
    }

}