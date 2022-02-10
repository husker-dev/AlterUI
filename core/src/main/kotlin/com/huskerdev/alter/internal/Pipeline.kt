package com.huskerdev.alter.internal

import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.OS
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Painter
import com.huskerdev.alter.internal.pipelines.d3d9.D3D9Pipeline
import com.huskerdev.alter.internal.pipelines.gl.GLPipeline
import com.huskerdev.alter.internal.utils.LibraryLoader
import com.huskerdev.alter.internal.utils.MainThreadLocker
import java.util.concurrent.TimeUnit

abstract class Pipeline {

    companion object {
        val current by lazy {
            return@lazy when(val pipelineName = AlterUIProperties.pipeline){
                "gl" -> GLPipeline()
                "d3d9" -> D3D9Pipeline()
                else -> {
                    // Search by name using reflection
                    Class.forName("com.huskerdev.alter.internal.pipelines.$pipelineName.${pipelineName.uppercase()}Pipeline")
                        .getDeclaredConstructor()
                        .newInstance() as Pipeline
                }
            }
        }

        val windows = arrayListOf<Window>()

        fun initialize(){
            current.load()
        }
    }

    abstract fun load()
    abstract fun createWindow(): Window
    abstract fun createGraphics(window: Window): Graphics
    abstract fun isUIRequireMainThread(): Boolean

    protected fun loadDefaultLibrary(name: String){
        val postfix = when(OS.current){
            OS.Windows -> ".dll"
            else -> throw UnsupportedOperationException("Unsupported OS")
        }
        LibraryLoader.load("com/huskerdev/alter/resources/${name}_${OS.arch.shortName}$postfix")
    }

    abstract class WindowPoll(private val libName: String): Pipeline() {

        override fun load() {
            loadDefaultLibrary(libName)

            MainThreadLocker.invokeAsync {
                MainThreadLocker.queueNotifiers.add {
                    if(windows.size > 0)
                        Platform.current.sendEmptyMessage(windows[0].handle)
                }
                while(true) {
                    if(MainThreadLocker.disposed)
                        break
                    if(MainThreadLocker.tasksQueue.size > 0)
                        MainThreadLocker.tasksQueue.poll(1, TimeUnit.MILLISECONDS)!!.run()

                    if(windows.isNotEmpty())
                        Platform.current.pollEvents()
                }
            }
        }
    }
}