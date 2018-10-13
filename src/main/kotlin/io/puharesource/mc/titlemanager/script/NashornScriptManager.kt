package io.puharesource.mc.titlemanager.script

import com.google.common.io.Resources
import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.isTesting
import io.puharesource.mc.titlemanager.pluginInstance
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class NashornScriptManager : ScriptManager {
    private val engine : ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    private val scripts : ConcurrentSkipListSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    override val registeredScripts: Set<String>
        get() = scripts.clone()

    init {
        engine.put("ScriptCommandSender", ScriptCommandSender::class.java)

        addResource("titlemanager_engine.js")

        registerAnimation("count_down")
        registerAnimation("count_up")
        registerAnimation("text_delete")
        registerAnimation("text_write")
        registerAnimation("shine")
        registerAnimation("marquee")
        registerAnimation("repeat")
    }

    private fun addResource(name: String) {
        if (isTesting) {
            engine.eval(Resources.getResource(name).readText())
        } else {
            engine.eval(pluginInstance.getResource(name).bufferedReader().readText())
        }
    }

    private fun registerAnimation(name: String) {
        addResource("animations/$name.js")
        scripts.add(name)
    }

    override fun addScript(name: String, script: String) {
        engine.eval(script)
        scripts.add(name )
    }

    override fun getFrameFromScript(name: String, text: String, index: Int): Array<*> {
        return (engine as Invocable).invokeFunction(name, text, index) as Array<*>
    }

    override fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean): Animation {
        return Animation {
            return@Animation object : Iterator<AnimationFrame> {
                var i = AtomicInteger(0)
                var done = false

                override fun hasNext() = !done

                override fun next(): AnimationFrame {
                    val str = if (withPlaceholders) APIProvider.replaceText(it, text) else text
                    val result = getFrameFromScript(name, str, i.getAndIncrement())

                    done = result[1] as Boolean

                    return StandardAnimationFrame(result[0] as String, (result[2] as Number).toInt(), (result[3] as Number).toInt(), (result[4] as Int).toInt())
                }
            }
        }
    }
}