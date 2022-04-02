package com.huskerdev.alter.demo

import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.Frame
import com.huskerdev.alter.components.Button
import com.huskerdev.alter.components.FlowPane
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.utils.LibraryLoader
import kotlin.concurrent.thread

fun main(args: Array<String>) = AlterUI.run {
    if("dev" in args)
        LibraryLoader.forceWrite = true

    val frame = Frame()
    frame.content = object: FlowPane(){
        init {
            children.add(Button())
        }
    }

    frame.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    frame.x = 200f
    frame.y = 200f
    frame.width = 500f
    frame.height = 500f
    frame.visible = true

}