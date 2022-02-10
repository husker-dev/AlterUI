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

    protected fun loadDefaultLibrary(){
        val archName = when(OS.arch){
            OS.Arch.X64 -> "x64"
            OS.Arch.X86 -> "x86"
            OS.Arch.Arm64 -> "arm64"
            else -> ""
        }
        val postfix = when(OS.current){
            OS.Windows -> ".dll"
            else -> throw UnsupportedOperationException("Unsupported OS")
        }

        LibraryLoader.load("com/huskerdev/alter/resources/win_$archName$postfix")
    }
}