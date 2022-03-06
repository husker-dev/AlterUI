import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.components.Frame
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.font.Font
import com.huskerdev.alter.graphics.Graphics
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.utils.LibraryLoader
import java.io.File
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


    Font.register(File("C:\\Users\\redfa\\Desktop\\Whitney-Medium.ttf"))

    Runtime.getRuntime().addShutdownHook(Thread{
        println("JVM shutdown")
    })

    var firstImage: Image? = null

    val window = object: Frame(){
        override fun paint(gr: Graphics) {
            super.paint(gr)

            gr.color = Color.black
            if(firstImage != null)
                gr.drawImage(firstImage!!, 0f, 0f, width, height)

            var y = 0
            for(i in 10..100 step 10){
                gr.font = Font.get("lobster").derived(i.toFloat())

                gr.drawText("This is lobster", 100f, 100f + y)
                y += i + 2
            }
        }
    }
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"

    window.background = Color.white
    window.visible = true

    thread {
        firstImage = Image.create("C:\\Users\\redfa\\Desktop\\Td7NHeUSZ9E.jpg")
        firstImage!!.linearFiltered = false
        window.repaint()

    }

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} sec")


}
