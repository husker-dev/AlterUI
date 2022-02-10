import com.huskerdev.alter.AlterUI
import com.huskerdev.alter.AlterUIProperties
import com.huskerdev.alter.graphics.Color
import com.huskerdev.alter.internal.Pipeline
import com.huskerdev.alter.internal.utils.LibraryLoader

fun main() = AlterUI.takeMain {
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/gl_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\gl\\out\\gl_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/d3d9_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\d3d9\\out\\d3d9_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/d3d11_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\d3d11\\out\\d3d11_x64.dll"
    LibraryLoader.alternativePaths["com/huskerdev/alter/resources/win_x64.dll"] =
        "C:\\Users\\redfa\\Documents\\Java_projects\\alterui\\native\\modules\\win\\out\\win_x64.dll"

    println("Pipeline: \t\t${AlterUIProperties.pipeline.uppercase()}")

    val start = System.nanoTime()
    AlterUI.load()

    val window = Pipeline.current.createWindow()
    window.title = "${AlterUIProperties.pipeline.uppercase()} Window"
    window.x = 200.0
    window.y = 200.0
    window.width = 500.0
    window.height = 500.0
    window.background = Color.rgb(0.9f, 0.9f, 0f)
    window.visible = true

    val current = System.nanoTime()
    println("Startup time: \t${(current - start) / 1000000.0 / 1000} ms")
}