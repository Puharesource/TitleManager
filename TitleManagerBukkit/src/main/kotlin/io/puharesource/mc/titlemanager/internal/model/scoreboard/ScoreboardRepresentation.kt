package io.puharesource.mc.titlemanager.internal.model.scoreboard

import io.puharesource.mc.titlemanager.internal.pluginConfig
import java.math.BigInteger
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardRepresentation(title: String = "", private val lines: MutableMap<Int, String> = ConcurrentHashMap()) {
    companion object {
        private val random = Random()

        private fun generateRandomString(): String = BigInteger(80, random).toString(32)
    }

    val isUpdatePending = AtomicBoolean(false)
    var name: String = generateRandomString()

    var title: String = ""
        set(value) {
            if (field != value || !pluginConfig.bandwidth.preventDuplicatePackets) {
                isUpdatePending.set(true)
            }

            field = value
        }

    init {
        this.title = title
    }

    fun generateNewScoreboardName() {
        name = generateRandomString()
    }

    val size: Int
        get() = lines.size

    fun get(index: Int) = lines[index]

    fun set(index: Int, text: String) {
        val isNew = lines[index] != text

        lines[index] = text

        if (isNew || !pluginConfig.bandwidth.preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }

    fun remove(index: Int) {
        val existed = lines.remove(index) != null

        if (existed || !pluginConfig.bandwidth.preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }
}
