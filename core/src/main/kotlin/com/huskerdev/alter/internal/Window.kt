package com.huskerdev.alter.internal

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.utils.MainThreadLocker

abstract class Window(val handle: Long) {

    private var _background: Color = Color.white
    var background: Color
        get() = _background
        set(value) {
            _background = value
            repaint()
        }

    private var _visible = false
    var visible: Boolean
        get() = _visible
        set(value) {
            _visible = value
            invokeOnMainIfRequired { setVisibleImpl(value) }
            repaint()
        }

    private var _title = ""
    var title: String
        get() = _title
        set(value) {
            _title = value
            invokeOnMainIfRequired { setTitleImpl(value) }
        }

    private var _x = 0.0
    var x: Double
        get() = _x
        set(value) {
            _x = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _y = 0.0
    var y: Double
        get() = _y
        set(value) {
            _y = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _width = 0.0
    var width: Double
        get() = _width
        set(value) {
            _width = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _height = 0.0
    var height: Double
        get() = _height
        set(value) {
            _height = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    val graphics: Graphics

    init {
        this.initCallbacks()
        graphics = Pipeline.current.createGraphics(this)
        onDrawCallback()
    }

    protected abstract fun initCallbacks()
    protected abstract fun setVisibleImpl(visible: Boolean)
    protected abstract fun setTitleImpl(title: String)
    protected abstract fun setSizeImpl(x: Double, y: Double, width: Double, height: Double)
    protected abstract fun requestRepaint()

    private fun onDrawCallback(){
        if(!visible)
            return
        graphics.begin()
        paint(graphics)
        graphics.end()
    }

    private fun onClosedCallback(){
        println("Window closed")
        Pipeline.windows.remove(this)
    }

    private fun onResizedCallback(width: Int, height: Int){
        _width = width.toDouble()
        _height = height.toDouble()
    }

    private fun onMovedCallback(x: Int, y: Int){
        _x = x.toDouble()
        _y = y.toDouble()
    }

    private fun invokeOnMainIfRequired(run: () -> Unit){
        if(Pipeline.current.isUIRequireMainThread())
            MainThreadLocker.invoke(run)
        else run()
    }

    private fun repaint(){
        invokeOnMainIfRequired { requestRepaint() }
    }

    fun paint(gr: Graphics){
        gr.clear()
        gr.color = background
        gr.fillRect(0f, 0f, width.toFloat(), height.toFloat())

        gr.color = Color.black
        gr.fillRect(0f, 0f, 200f, 200f)

        gr.color = Color.blue
        gr.fillRect(width.toFloat() - 200f, 0f, 200f, 200f)

        gr.color = Color.red
        gr.fillRect(width.toFloat() - 200f, height.toFloat() - 200f, 200f, 200f)

        gr.color = Color.green
        gr.fillRect(0f, height.toFloat() - 200f, 200f, 200f)

        gr.color = Color.white
        gr.fillRect(200f, 200f, width.toFloat() - 400f, height.toFloat() - 400f)
    }
}