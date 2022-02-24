package com.huskerdev.alter.internal

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker

abstract class Window(val handle: Long) {

    companion object {
        private fun invokeOnMainIfRequired(run: () -> Unit){
            if(Pipeline.current.isMainThreadRequired())
                MainThreadLocker.invoke(run)
            else run()
        }
    }

    var background = Color.white
        set(value) {
            field = value
            repaint()
        }

    var visible = false
        set(value) {
            field = value
            invokeOnMainIfRequired { setVisibleImpl(value) }
            repaint()
        }

    var title: String = ""
        set(value) {
            field = value
            invokeOnMainIfRequired { setTitleImpl(value) }
        }

    private var _x = 0
    var x: Int
        get() = _x
        set(value) {
            _x = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _y = 0
    var y: Int
        get() = _y
        set(value) {
            _y = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _width = 0
    var width: Int
        get() = _width
        set(value) {
            _width = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    private var _height = 0
    var height: Int
        get() = _height
        set(value) {
            _height = value
            invokeOnMainIfRequired { setSizeImpl(x, y, width, height) }
        }

    var physicalWidth = 0
        private set
    var physicalHeight = 0
        private set
    var physicalX = 0
        private set
    var physicalY = 0
        private set

    var dpi = 1f
        private set

    var onPaintEvent: (Graphics) -> Unit = {}
    var onClosedListeners = arrayListOf<() -> Unit>()
    var onResizedListeners = arrayListOf<() -> Unit>()
    var onMovedListeners = arrayListOf<() -> Unit>()
    var onDpiChangedListeners = arrayListOf<() -> Unit>()

    val graphics: Graphics

    init {
        this.initCallbacksImpl()
        dpi = getDpiImpl()
        graphics = Pipeline.current.createGraphics(this)
        onDrawCallback()
    }

    protected abstract fun initCallbacksImpl()
    protected abstract fun getDpiImpl(): Float
    protected abstract fun setVisibleImpl(visible: Boolean)
    protected abstract fun setTitleImpl(title: String)
    protected abstract fun setSizeImpl(x: Int, y: Int, width: Int, height: Int)
    protected abstract fun requestRepaint()

    fun repaint() = invokeOnMainIfRequired { requestRepaint() }

    private fun onDrawCallback(){
        if(!visible)
            return
        graphics.begin()
        onPaintEvent(graphics)
        graphics.end()
    }

    @ImplicitUsage
    private fun onClosedCallback(){
        Pipeline.windows.remove(this)
        onClosedListeners.forEach { it() }
    }

    @ImplicitUsage
    private fun onResizedCallback(width: Int, height: Int){
        _width = (width / dpi).toInt()
        _height = (height / dpi).toInt()
        physicalWidth = width
        physicalHeight = height
        onResizedListeners.forEach { it() }
    }

    @ImplicitUsage
    private fun onMovedCallback(x: Int, y: Int){
        _x = (x / dpi).toInt()
        _y = (y / dpi).toInt()
        physicalX = x
        physicalY = y
        onMovedListeners.forEach { it() }
    }

    @ImplicitUsage
    private fun onDpiChangedCallback(dpi: Float){
        this.dpi = dpi
        onDpiChangedListeners.forEach { it() }
    }

}