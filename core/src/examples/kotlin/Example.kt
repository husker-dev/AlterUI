import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.geom.Rectangle
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.graphics.Image
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.utils.LibraryLoader
import java.net.URL
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

    Runtime.getRuntime().addShutdownHook(Thread{
        println("JVM shutdown")
    })

    var firstImage: Image? = null
    var secondImage: Image? = null

    val window = Pipeline.current.createWindow()
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    window.x = 200.0
    window.y = 200.0
    window.width = 500.0
    window.height = 500.0
    window.background = Color.rgb(0.9f, 0.9f, 0f)

    window.apply {
        window.onRepaintEvent = { gr ->
            gr.clear()

            gr.color = background
            gr.fillRect(0f, 0f, width.toFloat(), height.toFloat())

            gr.color = Color.white
            gr.fillRect(100f, 100f, width.toFloat() - 200f, height.toFloat() - 200f)

            gr.color = Color.rgba(0f, 0f, 0f, 0.8f)
            gr.fillRect(0f, 0f, 200f, 200f)

            gr.color = Color.rgba(0f, 0f, 1f, 0.8f)
            gr.fillRect(width.toFloat() - 200f, 0f, 200f, 200f)

            gr.color = Color.rgba(1f, 0f, 0f, 0.8f)
            gr.fillRect(width.toFloat() - 200f, height.toFloat() - 200f, 200f, 200f)

            gr.color = Color.rgba(0f, 1f, 0f, 0.8f)
            gr.drawRect(0f, height.toFloat() - 200f, 200f, 200f)

            if(firstImage != null)
                gr.drawImage(firstImage!!, 0f, 0f, width.toFloat(), height.toFloat())

        }
    }
    window.visible = true


    thread {
        firstImage = Image.createFromURL("https://sun9-24.userapi.com/impg/LsVdTpA1ZtqYvMwOS8yJYQBCsIWAdvvKOnMOxQ/e0tIo-AiWwA.jpg?size=750x730&quality=96&sign=f4b42d8cebeed684793621ec62b04fb1&type=album")
        firstImage!!.linearFiltered = false
        window.requestRepaint()
    }

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} ms")
}
