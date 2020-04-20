package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.model.script.ScriptCommandSender
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptServiceNashorn @Inject constructor(private val plugin: TitleManagerPlugin, private val placeholderService: PlaceholderService) : ScriptService {
    private val animationsFolder = File(plugin.dataFolder, "animations")
    private val engine: ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    private val registeredScripts: ConcurrentSkipListSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    private val jsAnimationFileSequence: Sequence<File>
        get() = animationsFolder.listFiles()
                .asSequence()
                .filter { it.isFile }
                .filter { it.extension.equals("js", ignoreCase = true) }

    override val scripts: Set<String>
        get() = this.registeredScripts.clone()

    init {
        engine.put("ScriptCommandSender", ScriptCommandSender::class.java)

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
        engine.eval(script)
        this.registeredScripts.add(name)
    }

    override fun getFrameFromScript(name: String, text: String, index: Int) = (engine as Invocable).invokeFunction(name, text, index) as Array<*>

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

    override fun scriptExists(name: String): Boolean = this.registeredScripts.contains(name)

    private fun addResource(name: String) {
        plugin.getResource(name)?.let {
            engine.eval(it.bufferedReader().readText())
        }
    }

    private fun registerAnimation(name: String) {
        addResource("animations/$name.js")
        this.registeredScripts.add(name)
    }
}
