package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.model.script.ScriptCommandSender
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class ScriptServiceGraal @Inject constructor(private val plugin: TitleManagerPlugin, private val placeholderService: PlaceholderService) : ScriptService {
    override val engineName: String = "GraalVM"
    private val animationsFolder = File(plugin.dataFolder, "animations")
    private val context: Context = Context.newBuilder("js").allowAllAccess(true).build()
    private val registeredScripts: ConcurrentSkipListSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    private val jsAnimationFileSequence: Sequence<File>
        get() = animationsFolder.listFiles()
                .asSequence()
                .filter { it.isFile }
                .filter { it.extension.equals("js", ignoreCase = true) }

    override val scripts: Set<String>
        get() = this.registeredScripts.clone()

    init {
        context.polyglotBindings.putMember("ScriptCommandSender", ScriptCommandSender::class.java)

        addResource("titlemanager_engine.js")
    }

    override fun loadBuiltinScripts() {
        registerAnimation("count_down")
        registerAnimation("count_up")
        registerAnimation("text_delete")
        registerAnimation("text_write")
        registerAnimation("shine")
        registerAnimation("marquee")
        registerAnimation("repeat")
    }

    override fun loadScripts() {
        jsAnimationFileSequence.forEach {
            val name = it.nameWithoutExtension

            addScript(name, it)
        }
    }

    override fun addScript(name: String, script: String) {
        context.eval("js", script)
        this.registeredScripts.add(name)
    }

    override fun getFrameFromScript(name: String, text: String, index: Int): Array<Value> {
        val valueArray = context.eval("js", "$name('${text.replace("'", "\\'")}', $index)")
        val convertedArray = arrayOfNulls<Value>(valueArray.arraySize.toInt())

        for (i in 0 until valueArray.arraySize) {
            convertedArray[i.toInt()] = valueArray.getArrayElement(i)
        }

        return convertedArray as Array<Value>
    }

    override fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean) = Animation {
        return@Animation object : Iterator<AnimationFrame> {
            var i = AtomicInteger(0)
            var done = false

            override fun hasNext() = !done

            override fun next(): AnimationFrame {
                val str = if (withPlaceholders) placeholderService.replaceText(it, text) else text
                val result = getFrameFromScript(name, str, i.getAndIncrement())

                done = result[1].asBoolean()

                return StandardAnimationFrame(result[0].asString(), result[2].asInt(), result[3].asInt(), result[4].asInt())
            }
        }
    }

    override fun scriptExists(name: String): Boolean = this.registeredScripts.contains(name)

    private fun addResource(name: String) {
        context.eval("js", plugin.getResource(name)!!.bufferedReader().readText())
    }

    private fun registerAnimation(name: String) {
        addResource("animations/$name.js")
        this.registeredScripts.add(name)
    }
}
