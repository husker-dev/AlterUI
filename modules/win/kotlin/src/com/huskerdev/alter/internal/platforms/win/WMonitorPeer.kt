package com.huskerdev.alter.internal.platforms.win

import com.huskerdev.alter.Monitor
import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.geom.Rectangle

class WMonitorPeer(handle: Long): Monitor.EDIDMonitor() {

    companion object {
        @JvmStatic external fun nGetPrimary(): Long
        @JvmStatic external fun nGetAll(): LongArray
        @JvmStatic external fun nGetEDID(handle: Long): ByteArray
        @JvmStatic external fun nGetWidth(handle: Long): Int
        @JvmStatic external fun nGetHeight(handle: Long): Int
        @JvmStatic external fun nGetX(handle: Long): Int
        @JvmStatic external fun nGetY(handle: Long): Int
        @JvmStatic external fun nGetDpi(handle: Long): Float

        val primary: Monitor
            get() = WMonitorPeer(nGetPrimary())

        val list: Array<Monitor>
            get() = nGetAll().map { WMonitorPeer(it) }.toTypedArray()
    }

    override val edid by lazy { nGetEDID(handle) }

    override val position by lazy {
        Point(
            nGetX(handle).toFloat(),
            nGetY(handle).toFloat()
        )
    }

    override val size by lazy {
        Point(
            nGetWidth(handle).toFloat(),
            nGetHeight(handle).toFloat()
        )
    }

    override val dpi by lazy { nGetDpi(handle) }
}