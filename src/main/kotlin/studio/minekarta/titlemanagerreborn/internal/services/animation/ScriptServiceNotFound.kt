package studio.minekarta.titlemanagerreborn.internal.services.animation

import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.api.v2.animation.Animation
import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationFrame
import studio.minekarta.titlemanagerreborn.internal.model.animation.StandardAnimationFrame
import studio.minekarta.titlemanagerreborn.internal.model.script.BuiltinScripts
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import java.util.concurrent.atomic.AtomicInteger

class ScriptServiceNotFound(
    private val plugin: TitleManagerReborn,
    private val placeholderService: PlaceholderService,
    private val builtinScripts: BuiltinScripts
) : ScriptService {
    override val engineName: String = "NotFound"
    override val scripts: Set<String>
        get() = throw NotImplementedError()

    override fun loadScripts() {
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
