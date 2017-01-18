package io.puharesource.mc.titlemanager.scoreboard

import java.math.BigInteger
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardRepresentation(var title: String = "", private val lines: MutableMap<Int, String> = ConcurrentHashMap()) {
    private val random = Random()

    val isUpdatePending = AtomicBoolean(false)
    val isUsingPrimaryBoard = AtomicBoolean(true)

    var name : String = BigInteger(80, random).toString(32)

    fun generateNewScoreboardName() {
        name = BigInteger(80, random).toString(32)
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