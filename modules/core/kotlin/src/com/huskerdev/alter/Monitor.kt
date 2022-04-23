package com.huskerdev.alter

import com.huskerdev.alter.geom.Point
import com.huskerdev.alter.geom.Rectangle
import com.huskerdev.alter.internal.Platform

interface Monitor {

    companion object {
        val primary: Monitor
            get() = Platform.current.getPrimaryMonitor()

        val list: Array<Monitor>
            get() = Platform.current.getMonitors()
    }

    val name: String
    val size: Point
    val position: Point
    val dpi: Float

    abstract class EDIDMonitor: Monitor {

        override val name: String by lazy {
            for(i in 54..125 step 18){
                if(edid[i].toInt() != 0 || edid[i + 3].toInt() != -4)
                    continue

                var length = 17
                for(r in 5..17)
                    if(edid[i + r].toInt() == 10)
                        length = r - 5

                return@lazy String(edid.copyOfRange(i + 5, i + 5 + length))
            }
            return@lazy ""
        }

        abstract val edid: ByteArray
    }

}