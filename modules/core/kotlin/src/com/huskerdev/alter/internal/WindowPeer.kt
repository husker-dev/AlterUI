package com.huskerdev.alter.internal

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.utils.ImplicitUsage
import com.huskerdev.alter.internal.utils.MainThreadLocker
import com.huskerdev.alter.internal.utils.kotlin.unique

enum class WindowStatus {
    Default,
    Paused,
    Error,
    InProgress
}

enum class WindowHitPosition {
    Caption,
    Client,
    Transparent,
    // Buttons
    Minimize,
    Maximize,
    Close,
    // Borders
    Left,
    Right,
    Top,
    Bottom,
    // Corners
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
    // Default
    Default
}

abstract class WindowPeer(val handle: Long) {

    companion object {
        private fun invokeOnMainIfRequiredAsync(run: () -> Unit){
            if(Pipeline.current.isMainThreadRequired())
                MainThreadLocker.invokeAsync(run)
            else run()
        }
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

    open var status by unique(WindowStatus.Default){
        invokeOnMainIfRequired { setStatusImpl(it) }
    }

    open var progress by unique(-1f){
        invokeOnMainIfRequired { setProgressImpl(it) }
    }

    open var style by unique<WindowStyle>(WindowStyle.Default){
        invokeOnMainIfRequired { setStyleImpl(it) }
    }

    open var color by unique<Color?>(null){
        invokeOnMainIfRequired { setColorImpl(it) }
    }

    open var textColor by unique<Color?>(null){
        invokeOnMainIfRequired { setTextColorImpl(it) }
    }

    open val mousePosition: Point?
        get() = Point(getMouseXImpl().toFloat() / dpi, getMouseYImpl().toFloat() / dpi)

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
    var onClosingListeners = arrayListOf<() -> Boolean>()
    var onResizedListeners = arrayListOf<() -> Unit>()
    var onMovedListeners = arrayListOf<() -> Unit>()
    var onDpiChangedListeners = arrayListOf<() -> Unit>()
    var onMouseMovedListeners = arrayListOf<() -> Unit>()

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
    protected abstract fun setStyleImpl(style: WindowStyle)
    protected abstract fun setColorImpl(color: Color?)
    protected abstract fun setTextColorImpl(color: Color?)

    protected abstract fun getMouseXImpl(): Int
    protected abstract fun getMouseYImpl(): Int

    protected abstract fun requestRepaint()

    fun repaint() = invokeOnMainIfRequiredAsync { requestRepaint() }

    @ImplicitUsage
    fun onDrawCallback(){
        if(!visible)
            return
        graphics.reset()
        onPaintEvent(graphics)
        graphics.finish()
    }

    @ImplicitUsage
    fun onClosingCallback(): Boolean {
        var result = true
        onClosingListeners.forEach {
            if(!it())
                result = false
        }
        return result
    }

    @ImplicitUsage
    fun onClosedCallback(){
        Pipeline.windows.remove(this)
        onClosedListeners.forEach { it() }
    }

    @ImplicitUsage
    fun onResizedCallback(clientWidth: Int, clientHeight: Int, windowWidth: Int, windowHeight: Int){
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
    fun onMovedCallback(x: Int, y: Int){
        if(physicalX == x && physicalY == y)
            return
        _x = x / dpi
        _y = y / dpi
        physicalX = x
        physicalY = y
        onMovedListeners.forEach { it() }
    }

    @ImplicitUsage
    fun onDpiChangedCallback(dpi: Float){
        this.dpi = dpi
        onDpiChangedListeners.forEach { it() }
    }

    @ImplicitUsage
    fun onHitTestCallback(x: Int, y: Int) = style.hitTest(this, x, y).ordinal

    @ImplicitUsage
    fun onMouseMoved(x: Int, y: Int, leftDown: Boolean, middleDown: Boolean, rightDown: Boolean, ctrlDown: Boolean, shiftDown: Boolean) {
        onMouseMovedListeners.forEach { it() }
    }

    @ImplicitUsage
    fun onMouseEntered() {
        //onMouseMovedListeners.forEach { it() }
    }

    @ImplicitUsage
    fun onMouseLeaved() {
        //_mousePosition = Point(-1f, -1f)
        //onMouseMovedListeners.forEach { it() }
    }

}
