package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.model.script.BuiltinScripts
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import java.util.concurrent.atomic.AtomicInteger

class ScriptServiceNotFound(
    private val plugin: TitleManagerPlugin,
    private val placeholderService: PlaceholderService,
    private val builtinScripts: BuiltinScripts
) : ScriptService {
    override val engineName: String = "NotFound"
    override val scripts: Set<String>
        get() = throw NotImplementedError()

    override fun loadScripts() {
        plugin.logger.warning("No script engine found. Consider adding Nashorn or GraalVM to be able to create custom animation scripts. (This warning can be ignored)")
    }

    override fun addScript(name: String, script: String) {
        throw NotImplementedError()
    }

    override fun getFrameFromScript(name: String, text: String, index: Int): Array<*> {
        return builtinScripts[name]!!(text, index).asArray()
    }

    override fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean) = Animation {
        return@Animation object : Iterator<AnimationFrame> {
            var i = AtomicInteger(0)
            var done = false

            override fun hasNext() = !done

            override fun next(): AnimationFrame {
                val str = if (withPlaceholders) placeholderService.replaceText(it, text) else text
                val result = getFrameFromScript(name, str, i.getAndIncrement())

                done = result[1] as Boolean

                return StandardAnimationFrame(result[0] as String, (result[2] as Number).toInt(), (result[3] as Number).toInt(), (result[4] as Int).toInt())
            }
        }
    }

    override fun scriptExists(name: String): Boolean = builtinScripts.contains(name)
}
