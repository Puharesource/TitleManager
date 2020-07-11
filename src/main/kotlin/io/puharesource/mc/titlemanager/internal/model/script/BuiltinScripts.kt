package io.puharesource.mc.titlemanager.internal.model.script

import io.puharesource.mc.titlemanager.internal.model.script.builtin.CountDownScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.CountUpScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.GradientColorScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.GradientScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.MarqueeScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.RepeatScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.ShineScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.TextDeleteScript
import io.puharesource.mc.titlemanager.internal.model.script.builtin.TextWriteScript
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import java.util.concurrent.ConcurrentSkipListMap

class BuiltinScripts {
    private val scripts: MutableMap<String, (String, Int) -> ScriptResult> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    init {
        set("count_down") { text, index -> CountDownScript(text, index) }
        set("count_up") { text, index -> CountUpScript(text, index) }
        set("text_delete") { text, index -> TextDeleteScript(text, index) }
        set("text_write") { text, index -> TextWriteScript(text, index) }
        set("shine") { text, index -> ShineScript(text, index) }
        set("marquee") { text, index -> MarqueeScript(text, index) }
        set("repeat") { text, index -> RepeatScript(text, index) }
        set("gradient", NMSManager.versionIndex >= 10) { text, index -> GradientScript(text, index) }
        set("gradient_color", NMSManager.versionIndex >= 10) { text, index -> GradientColorScript(text, index) }
    }

    operator fun get(key: String): ((String, Int) -> ScriptResult)? = scripts[key]

    fun contains(key: String) = scripts.contains(key)

    private fun set(key: String, scriptGenerator: (text: String, index: Int) -> AnimationScript) {
        scripts[key] = { text, index ->
            val script = scriptGenerator(text, index)

            script.decode()
            script.generateFrame()

            script.result
        }
    }

    private fun set(key: String, enabled: Boolean, scriptGenerator: (text: String, index: Int) -> AnimationScript) {
        if (enabled) {
            set(key, scriptGenerator)
        }
    }
}
