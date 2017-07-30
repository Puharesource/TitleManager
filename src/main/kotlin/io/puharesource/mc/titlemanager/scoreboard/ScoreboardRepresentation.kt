package io.puharesource.mc.titlemanager.scoreboard

import io.puharesource.mc.titlemanager.generateRandomString
import io.puharesource.mc.titlemanager.pluginInstance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardRepresentation(title: String = "", private val lines: MutableMap<Int, String> = ConcurrentHashMap()) {
    val isUpdatePending = AtomicBoolean(false)
    var name : String = generateRandomString()

    var title : String = ""
        set(value) {
            if (field != value || !preventDuplicatePackets) {
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

    private val preventDuplicatePackets: Boolean
        get() = pluginInstance.config.getBoolean("bandwidth.prevent-duplicate-packets")

    val size : Int
        get() = lines.size

    fun get(index: Int) = lines[index]

    fun set(index: Int, text: String) {
        val isNew = lines[index] != text

        if (text.length > 40) {
            lines[index] = text.substring(0, 40)
        } else {
            lines[index] = text
        }

        if (isNew || !preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }

    fun remove(index: Int) {
        val existed = lines.remove(index) != null

        if (existed || !preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }
}