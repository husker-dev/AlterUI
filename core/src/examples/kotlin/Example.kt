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


            if(secondImage != null)
                gr.drawImage(secondImage!!, width.toFloat() - 300f, 0f, 300f, 300f)

        }
    }

    window.visible = true

    thread {
        secondImage = Image.createFromURL("https://sun9-57.userapi.com/impg/1bZ2JejlcpxeEiIUd7OEcbyjJ5XpOvdhuOrKew/qigWWTeP694.jpg?size=1242x1515&quality=96&sign=3696360abe9d16443f11887a0ff78b82&type=album")
        window.requestRepaint()
    }

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} ms")


}
