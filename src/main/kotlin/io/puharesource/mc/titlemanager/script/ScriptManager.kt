package io.puharesource.mc.titlemanager.script

import com.google.common.io.Resources
import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.isTesting
import io.puharesource.mc.titlemanager.pluginInstance
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

object ScriptManager {
    private var context : Context = Context.newBuilder("js").allowAllAccess(true).build()
    internal val registeredScripts : MutableSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    init {
        reloadInternals()
    }

    fun reloadInternals() {
        context = Context.newBuilder("js").allowAllAccess(true).build()

        fun addResource(file: String) {
            if (isTesting) {
                context.eval("js", Resources.getResource(file).readText())
            } else {
                context.eval("js", pluginInstance.getResource(file).bufferedReader().readText())
            }
        }

        fun registerAnimation(name: String) {
            addResource("animations/$name.js")
            registeredScripts.add(name)
        }

        context.polyglotBindings.putMember("ScriptCommandSender", ScriptCommandSender::class.java)

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
        context.eval("js", js)
    }

    fun addJavaScript(file: File) {
        addJavaScript(file.bufferedReader().readText())
    }

    fun getFrameFromScript(name: String, text: String, index: Int) : Array<Value> {
        val valueArray = context.eval("js", "$name('${text.replace("'", "\\'")}', $index)")
        val convertedArray = arrayOfNulls<Value>(valueArray.arraySize.toInt())

        for (i in 0 until valueArray.arraySize) {
            convertedArray[i.toInt()] = valueArray.getArrayElement(i)
        }

        return convertedArray as Array<Value>
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

                    done = result[1].asBoolean()

                    return StandardAnimationFrame(result[0].asString(), result[2].asInt(), result[3].asInt(), result[4].asInt())
                }
            }
        }
    }
}