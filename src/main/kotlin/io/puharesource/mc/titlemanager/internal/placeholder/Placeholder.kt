package io.puharesource.mc.titlemanager.internal.placeholder

import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

abstract class Placeholder(vararg val aliases: String) {
    private var cacheTime: Long? = null
    private var cacheUnit: TimeUnit = TimeUnit.SECONDS
    private var lastCached: Long = 0 // TODO: Make this take the player and value into consideration
    private var cachedValue: String? = null

    open val isEnabled
        get() = true

    private val isCacheEnabled
        get() = cacheTime != null

    abstract fun getText(player: Player, value: String?): String

    protected fun processOutput(output: Any): String {
        if (isCacheEnabled && !isNewValueRequired()) {
            return cachedValue.orEmpty()
        }

        val outputString = if (output is String) {
            output
        } else {
            output.toString()
        }

        if (isCacheEnabled) {
            cachedValue = outputString
            lastCached = System.currentTimeMillis()
        }

        return outputString
    }

    private fun isNewValueRequired(): Boolean {
        return cacheTime == null || lastCached + cacheUnit.toMillis(cacheTime!!) <= System.currentTimeMillis()
    }

    fun cached(time: Long): Placeholder {
        cacheTime = time

        return this
    }

    fun cached(time: Long, unit: TimeUnit): Placeholder {
        cacheTime = time
        cacheUnit = unit

        return this
    }
}
