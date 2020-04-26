package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation

class ScriptServiceNotFound : ScriptService {
    override val engineName: String = "NotFound"
    override val scripts: Set<String>
        get() = throw NotImplementedError()

    override fun loadBuiltinScripts() {
        throw NotImplementedError()
    }

    override fun loadScripts() {
        throw NotImplementedError()
    }

    override fun addScript(name: String, script: String) {
        throw NotImplementedError()
    }

    override fun getFrameFromScript(name: String, text: String, index: Int): Array<*> {
        throw NotImplementedError()
    }

    override fun getScriptAnimation(name: String, text: String, withPlaceholders: Boolean): Animation {
        throw NotImplementedError()
    }

    override fun scriptExists(name: String): Boolean {
        throw NotImplementedError()
    }
}
