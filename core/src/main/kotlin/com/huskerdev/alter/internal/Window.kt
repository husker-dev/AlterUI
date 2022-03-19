package com.huskerdev.alter.internal

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker

enum class WindowStatus {
    Default,
    Paused,
    Error,
    InProgress
}

abstract class Window(val handle: Long) {

    companion object {
        private inline fun invokeOnMainIfRequired(crossinline run: () -> Unit){
            if(Pipeline.current.isMainThreadRequired())
                MainThreadLocker.invoke(run)
            else run()
        }
    }

    open var background = Color.white
        set(value) {
            field = value
            repaint()
        }

    open var visible = false
        set(value) {
            field = value
            invokeOnMainIfRequired { setVisibleImpl(value) }
            repaint()
        }

    open var title: String = ""
        set(value) {
            field = value
            invokeOnMainIfRequired { setTitleImpl(value) }
        }

    open var icon: Image? = null
        set(value) {
            field = value
            invokeOnMainIfRequired { setIconImpl(value) }
        }

    private var _x = 0f
    open var x: Float
        get() = _x
        set(value) {
            _x = value
            invokeOnMainIfRequired { setSizeImpl((value * dpi).toInt(), physicalY, physicalWidth, physicalHeight) }
        }

    private var _y = 0f
    open var y: Float
        get() = _y
        set(value) {
            _y = value
            invokeOnMainIfRequired { setSizeImpl(physicalX, (value * dpi).toInt(), physicalWidth, physicalHeight) }
        }

    private var _width = 0f
    open var width: Float
        get() = _width
        set(value) {
            _width = value
            invokeOnMainIfRequired { setSizeImpl(physicalX, physicalY, (value * dpi).toInt(), physicalHeight) }
        }

    private var _height = 0f
    open var height: Float
        get() = _height
        set(value) {
            _height = value
            invokeOnMainIfRequired { setSizeImpl(physicalX, physicalY, physicalWidth, (value * dpi).toInt()) }
        }

    private var _status = WindowStatus.Default
    open var status: WindowStatus
        get() = _status
        set(value) {
            _status = value
            invokeOnMainIfRequired { setStatusImpl(value) }
        }

    private var _progress = -1f
    open var progress: Float
        get() = _progress
        set(value) {
            _progress = value
            invokeOnMainIfRequired { setProgressImpl(value) }
        }

    var clientWidth = 0
        private set
    var clientHeight = 0
        private set
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

        invokeOnMainIfRequired {
            setSizeImpl(
                (100f * dpi).toInt(),
                (100f * dpi).toInt(),
                (300f * dpi).toInt(),
                (300f * dpi).toInt()
            )
        }
    }

    protected abstract fun initCallbacksImpl()
    protected abstract fun getDpiImpl(): Float
    protected abstract fun setVisibleImpl(visible: Boolean)
    protected abstract fun setTitleImpl(title: String)
    protected abstract fun setSizeImpl(x: Int, y: Int, width: Int, height: Int)
    protected abstract fun setIconImpl(image: Image?)
    protected abstract fun setStatusImpl(status: WindowStatus)
    protected abstract fun setProgressImpl(progress: Float)

    protected abstract fun requestRepaint()

    fun repaint() = invokeOnMainIfRequired { requestRepaint() }

    @ImplicitUsage
    private fun onDrawCallback(){
        if(!visible)
            return
        graphics.reset()
        onPaintEvent(graphics)
        graphics.flush()
    }

    @ImplicitUsage
    private fun onClosedCallback(){
        Pipeline.windows.remove(this)
        onClosedListeners.forEach { it() }
    }

    @ImplicitUsage
    private fun onResizedCallback(clientWidth: Int, clientHeight: Int, windowWidth: Int, windowHeight: Int){
        if(windowWidth == physicalWidth && windowHeight == physicalHeight)
            return
        _width = clientWidth.toFloat() / dpi
        _height = clientHeight.toFloat() / dpi
        this.physicalWidth = windowWidth
        this.physicalHeight = windowHeight
        this.clientWidth = clientWidth
        this.clientHeight = clientHeight
        onResizedListeners.forEach { it() }
    }

    @ImplicitUsage
    private fun onMovedCallback(x: Int, y: Int){
        if(physicalX == x && physicalY == y)
            return
        _x = x / dpi
        _y = y / dpi
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