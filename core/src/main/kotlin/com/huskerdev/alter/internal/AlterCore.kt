package com.huskerdev.alter.internal

import com.huskerdev.alter.internal.utils.LibraryLoader
import java.nio.ByteBuffer

class AlterCore {

    companion object {
        @JvmStatic external fun nGetBitmapInfo(data: ByteBuffer): IntArray
        @JvmStatic external fun nGetBitmap(data: ByteBuffer): ByteBuffer
        @JvmStatic external fun nGetBitmapInfoFromFile(path: ByteBuffer): IntArray
        @JvmStatic external fun nGetBitmapFromFile(path: ByteBuffer): ByteBuffer
        @JvmStatic external fun nReleaseBitmap(data: ByteBuffer)

        fun initialize(){
            LibraryLoader.loadModuleLib("base")
        }
    }
}