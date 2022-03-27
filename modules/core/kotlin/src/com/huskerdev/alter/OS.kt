package com.huskerdev.alter


enum class OS(val shortName: String) {

    Windows("win"),
    MacOS("mac"),
    Linux("linux"),
    Other("other");

    enum class Arch(val shortName: String) {
        X64("x64"),
        X86("x86"),
        Arm64("arm64")
    }

    companion object {
        val current by lazy {
            val name = System.getProperty("os.name", "generic").lowercase()
            return@lazy if ("mac" in name || "darwin" in name)
                MacOS
            else if ("win" in name)
                Windows
            else if ("nux" in name)
                Linux
            else Other
        }

        val arch by lazy {
            if(current == Windows){
                when(System.getenv("PROCESSOR_ARCHITECTURE").lowercase()){
                    "amd64", "ia64", "em64t" -> Arch.X64
                    "x86" -> Arch.X86
                    "arm64" -> Arch.Arm64
                    else -> throw UnsupportedOperationException("Unsupported processor architecture")
                }
            }else
                throw UnsupportedOperationException("Unsupported OS")
        }
    }
}