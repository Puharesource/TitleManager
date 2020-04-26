package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import java.io.File

interface ScriptService {
    val engineName: String
    val scripts: Set<String>

    fun loadBuiltinScripts()
    fun loadScripts()

    fun addScript(name: String, script: String)
    fun addScript(name: String, scriptFile: File) = addScript(name, scriptFile.readText())
    fun getFrameFromScript(name: String, text: String, index: Int): Array<*>
    fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean = false): Animation
    fun scriptExists(name: String): Boolean
}
