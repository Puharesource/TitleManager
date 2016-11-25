package io.puharesource.mc.titlemanager.script

import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.pluginInstance
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object ScriptManager {
    private val javaScriptEngine : ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")

    init {
        javaScriptEngine.eval(pluginInstance.getResource("titlemanager_engine.js").reader())
    }

    fun addJavaScript(file: File) {
        javaScriptEngine.eval(file.reader())
    }

    fun getJavaScriptAnimation(name: String, text: String, withPlaceholders: Boolean = false) : Animation {
        val invocable = javaScriptEngine as Invocable

        return Animation {
            return@Animation object : Iterator<AnimationFrame> {
                var i = AtomicInteger(0)
                var done = false

                override fun hasNext(): Boolean {
                    return !done
                }

                override fun next(): AnimationFrame {
                    val str = if (withPlaceholders) pluginInstance.replaceText(it, text) else text
                    val result = invocable.invokeFunction(name, str, i.getAndIncrement()) as Array<*>

                    done = result[1] as Boolean

                    return StandardAnimationFrame(result[0] as String, result[2] as Int, result[3] as Int, result[4] as Int)
                }
            }
        }
    }
}