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
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

class GraalScriptManager : ScriptManager {
    private val context : Context = Context.newBuilder("js").allowAllAccess(true).build()
    private val scripts : ConcurrentSkipListSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    override val registeredScripts: Set<String>
        get() = scripts.clone()

    init {
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

    private fun addResource(name: String) {
        if (isTesting) {
            context.eval("js", Resources.getResource(name).readText())
        } else {
            context.eval("js", pluginInstance.getResource(name).bufferedReader().readText())
        }
    }

    private fun registerAnimation(name: String) {
        addResource("animations/$name.js")
        scripts.add(name)
    }

    override fun addScript(name: String, script: String) {
        context.eval("js", script)
        scripts.add(name)
    }

    override fun getFrameFromScript(name: String, text: String, index: Int) : Array<Value> {
        val valueArray = context.eval("js", "$name('${text.replace("'", "\\'")}', $index)")
        val convertedArray = arrayOfNulls<Value>(valueArray.arraySize.toInt())

        for (i in 0 until valueArray.arraySize) {
            convertedArray[i.toInt()] = valueArray.getArrayElement(i)
        }

        return convertedArray as Array<Value>
    }

    override fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean) : Animation {
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