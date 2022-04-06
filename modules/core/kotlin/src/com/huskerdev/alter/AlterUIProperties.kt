package com.huskerdev.alter


class AlterUIProperties {
    companion object {

        /**
         *  Rendering pipeline (default: gl)
         */
        @JvmStatic var pipeline = System.getProperty("alterui.pipeline", "gl")!!

        /**
         *  Force library caching, even if it's already cached
         */
        @JvmStatic var forceLibraryCaching = System.getProperty("alterui.forceLibCaching", "false").toBoolean()

        /**
         *  Vertical synchronisation
         */
        @JvmStatic var vsync = System.getProperty("alterui.vsync", "false").toBoolean()

        /**
         *  Maximum frames per second
         */
        @JvmStatic var fpsLimit = System.getProperty("alterui.fpsLimit", "-1").toInt()

        /**
         *  Force application to repaint without stopping
         */
        @JvmStatic var alwaysRepaint = System.getProperty("alterui.alwaysRepaint", "false").toBoolean()


    }
}