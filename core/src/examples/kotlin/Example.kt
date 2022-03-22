import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.WindowStyle
import com.huskerdev.alter.internal.utils.LibraryLoader
import kotlin.concurrent.thread

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

    var image: Image? = null

    val window = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            gr.color = Color.white
            if(image != null) {
                //gr.drawImage(image!!, 150f, 150f, 100f, 100f)
                //gr.drawImage(image!!, width - 100f, height - 100f, 100f, 100f)
            }
        }
    }
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    //window.icon = Image.create("C:\\Users\\redfa\\Desktop\\Check_green_icon.svg.png")
    window.background = Color.white

    //window.style = WindowStyle.NoTitle

    window.visible = true


    thread {
        image = Image.fromFile("C:\\Users\\redfa\\Desktop\\15104f78cb83e3cefaf63ecc718a2a43.jpg")
        window.repaint()

    }

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} sec")


}
