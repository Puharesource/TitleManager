package io.puharesource.mc.titlemanager.internal.model.script

data class ScriptResult(val text: String, val done: Boolean, val fadeIn: Int = 0, val stay: Int = 20, val fadeOut: Int = 0) {
    fun asArray() = arrayOf(text, done, fadeIn, stay, fadeOut)
}
