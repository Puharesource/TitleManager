package io.puharesource.mc.titlemanager.scoreboard

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardRepresentation(var title: String = "", private val lines: MutableMap<Int, String> = ConcurrentHashMap()) {
    val isUpdatePending = AtomicBoolean(false)
    val isUsingPrimaryBoard = AtomicBoolean(true)

    var name : String = System.nanoTime().toString()

    fun generateNewScoreboardName() {
        name = System.nanoTime().toString()
    }

    val size : Int
        get() = lines.size

    fun get(index: Int) = lines[index]

    fun set(index: Int, text: String) {
        if (text.length > 40) {
            lines[index] = text.substring(0, 40)
        } else {
            lines[index] = text
        }

        isUpdatePending.set(true)
    }

    fun remove(index: Int) {
        lines.remove(index)

        isUpdatePending.set(true)
    }
}