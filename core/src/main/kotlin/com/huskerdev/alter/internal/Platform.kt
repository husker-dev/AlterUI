package com.huskerdev.alter.internal

import com.huskerdev.alter.OS
import com.huskerdev.alter.internal.platforms.win.WindowsPlatform
import com.huskerdev.alter.internal.utils.LibraryLoader

val String.c_str: ByteArray
    get() = encodeToByteArray().c_str

val ByteArray.c_str: ByteArray
    get() {
        val cBytes = ByteArray(this.size + 1)
        System.arraycopy(this, 0, cBytes, 0, this.size)
        return cBytes
    }

abstract class Platform {

    companion object {

        val current by lazy {
            when(OS.current){
                OS.Windows -> WindowsPlatform()
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
        }

        fun initialize(){
            current.load()
        }
    }

    abstract fun load()
    abstract fun createWindowInstance(handle: Long): Window
    abstract fun pollEvents()
    abstract fun sendEmptyMessage(handle: Long)

}