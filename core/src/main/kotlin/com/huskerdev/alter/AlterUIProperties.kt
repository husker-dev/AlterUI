package com.huskerdev.alter

/**
 *  Properties:
 *      - alter.pipeline   - Rendering pipeline (default: gl)
 */
class AlterUIProperties {

    companion object {

        private var cPipeline = "gl"
        @JvmStatic var pipeline: String
            get() = System.getProperty("alter.pipeline", cPipeline)
            set(value) {
                cPipeline = value
            }

    }
}