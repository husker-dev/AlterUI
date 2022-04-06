package com.huskerdev.alter.components

import com.huskerdev.alter.graphics.Graphics
import kotlin.math.max

open class FlowPane(
    var vgap: Int = 5,
    var hgap: Int = 20
): Component() {

    override fun paintComponent(gr: Graphics) {}

    override fun doLayout() {
        var maxLineHeight = 0f
        val currentLineComponents = arrayListOf<Component>()

        var lineX = 0f
        var currentX = 0f
        var currentY = 0f
        for(child in children){
            if(currentX + child.preferredWidth + hgap > width && currentLineComponents.size > 0){
                // Layout current line
                val startX = (width - currentX) / 2f
                currentLineComponents.forEach {
                    it.x = startX + lineX
                    it.y = currentY
                    it.width = it.preferredWidth
                    it.height = it.preferredHeight

                    lineX += it.width + hgap
                }

                currentY += maxLineHeight + vgap
                maxLineHeight = 0f
                currentX = 0f
                lineX = 0f
                currentLineComponents.clear()
            }

            currentLineComponents.add(child)
            if(currentX != 0f)
                currentX += hgap
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

            lineX += it.width + hgap
        }
    }
}