package io.puharesource.mc.titlemanager.internal.script

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import java.io.File
import javax.script.ScriptEngineManager

interface ScriptManager {
    companion object {
        fun create(): ScriptManager? {
            if (Package.getPackage("org.graalvm") != null) {
                return GraalScriptManager()
            }

            if (ScriptEngineManager().getEngineByName("nashorn") != null) {
                return NashornScriptManager()
            }

            return null
        }
    }

    fun addScript(name: String, script: String)
    fun addScript(name: String, scriptFile: File) = addScript(name, scriptFile.readText())
    fun getFrameFromScript(name: String, text: String, index: Int) : Array<*>
    fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean = false) : Animation
    val registeredScripts: Set<String>
}