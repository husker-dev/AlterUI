package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import kotlin.math.max

open class FlowPane: Component() {
    override fun paintComponent(gr: Graphics) {}

    override fun doLayout() {
        var maxLineHeight = 0f
        var currentLineComponents = arrayListOf<Component>()

        var lineX = 0f
        var currentX = 0f
        var currentY = 0f
        for(child in children){
            if(currentX + child.preferredWidth > width){
                // Layout current line
                val startX = (width - currentX) / 2f
                currentLineComponents.forEach {
                    it.x = startX + lineX
                    it.y = currentY
                    it.width = it.preferredWidth
                    it.height = it.preferredHeight

                    lineX += it.width
                }

                currentY += maxLineHeight
                maxLineHeight = 0f
                currentX = 0f
                lineX = 0f
                currentLineComponents.clear()
            }

            currentLineComponents.add(child)
            currentX += child.preferredWidth
            maxLineHeight = max(maxLineHeight, child.preferredHeight)
        }

        // Layout last line
        val startX = (width - currentX) / 2f
        currentLineComponents.forEach {
            it.x = startX + lineX
            it.y = currentY
            it.width = it.preferredWidth
            it.height = it.preferredHeight

            lineX += it.width
        }
    }
}