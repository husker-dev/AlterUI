import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.graphics.ResizeAlgorithm
import com.huskerdev.alter.internal.utils.LibraryLoader
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.sin

fun main() = AlterUI.takeMain {
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/gl/gl_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\gl\\out\\gl_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/d3d9/d3d9_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\d3d9\\out\\d3d9_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/d3d11/d3d11_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\d3d11\\out\\d3d11_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/win/win_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\win\\out\\win_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/base/base_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\base\\out\\base_x64.dll"

    println("Pipeline: \t\t${AlterUIProperties.pipeline.uppercase()}")

    val start = System.nanoTime()
    AlterUI.load()

    var firstImage: Image? = null

    val window = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            //gr.color = Color.black
            //gr.fillRect(0f, 0f, 200f, 200f)

            gr.color = Color.white
            //gr.font = gr.font.derived(100f)

            //gr.drawText("Test", 50f, 50f)

            if(firstImage != null)
                gr.drawImage(firstImage!!, 50f, 50f, 100f, 100f)
        }
    }
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    //window.icon = Image.create("C:\\Users\\redfa\\Desktop\\Check_green_icon.svg.png")
    window.background = Color.blue
    window.visible = true

    thread {
        firstImage = Image.create("C:\\Users\\redfa\\Desktop\\15104f78cb83e3cefaf63ecc718a2a43.jpg")
        firstImage!!.writeToFile("aboba.png")
        window.icon = firstImage

        //firstImage = firstImage!!.getResized((32 * 1.75).toInt(), (32 * 1.75).toInt(), ResizeType.Mitchell)
        //firstImage!!.linearFiltered = false
        //firstImage = firstImage!!.getSubImage(0, 0, firstImage!!.width / 2, firstImage!!.height / 2)
        //window.icon = firstImage

        window.repaint()

        /*
        val graphics = firstImage!!.graphics
        graphics.color = Color.red
        graphics.fillRect(0f, 0f, firstImage!!.width / 2f, firstImage!!.height / 2f)
        graphics.flush()

         */


    }

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} sec")


}
