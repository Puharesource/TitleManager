package io.puharesource.mc.titlemanager.script

import com.google.common.io.Resources
import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.isTesting
import io.puharesource.mc.titlemanager.pluginInstance
import io.puharesource.mc.titlemanager.warning
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object ScriptManager {
    private var javaScriptEngine : ScriptEngine? = null
    internal val registeredScripts : MutableSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    init {
        reloadInternals()

        if (javaScriptEngine == null) {
            warning("Unable to initialize script engine! Scripts won't be working. This is probably due to running on OpenJDK rather than Oracle.")
        }
    }

    fun reloadInternals() {
        javaScriptEngine = ScriptEngineManager().getEngineByName("nashorn") ?: return

        fun addResource(file: String) {
            if (isTesting) {
                javaScriptEngine!!.eval(Resources.getResource(file).readText())
            } else {
                javaScriptEngine!!.eval(pluginInstance.getResource(file).reader())
            }
        }

        fun registerAnimation(name: String) {
            addResource("animations/$name.js")
            registeredScripts.add(name)
        }

        javaScriptEngine!!.put("ScriptCommandSender", ScriptCommandSender::class.java)

        addResource("titlemanager_engine.js")

        registerAnimation("count_down")
        registerAnimation("count_up")
        registerAnimation("text_delete")
        registerAnimation("text_write")
        registerAnimation("shine")
        registerAnimation("marquee")
        registerAnimation("repeat")
    }

    fun addJavaScript(js: String) {
        javaScriptEngine?.eval(js)
    }

    fun addJavaScript(file: File) {
        javaScriptEngine?.eval(file.reader())
    }

    fun getFrameFromScript(name: String, text: String, index: Int) : Array<*> {
        return (javaScriptEngine as Invocable).invokeFunction(name, text, index) as Array<*>
    }

    fun getJavaScriptAnimation(name: String, text: String, withPlaceholders: Boolean = false) : Animation {
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