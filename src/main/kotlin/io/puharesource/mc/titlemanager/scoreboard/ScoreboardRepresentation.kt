package io.puharesource.mc.titlemanager.scoreboard

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardRepresentation {
    private val lines : MutableMap<Int, String>

    val isUpdatePending = AtomicBoolean(false)
    val isUsingPrimaryBoard = AtomicBoolean(true)

    constructor(title: String = "", lines: MutableMap<Int, String> = ConcurrentHashMap()) {
        this.title = title
        this.lines = lines
    }

    var title : String
        set(value) {
            if (value.length > 32) {
                field = value.substring(0, 32)
            } else {
                field = value
            }
        }

    val size : Int
        get() = lines.size

    val currentScoreboardName : String
        get() = "titlemanager${if (isUsingPrimaryBoard.get()) 1 else 2}"

    val otherScoreboardName : String
        get() = "titlemanager${if (isUsingPrimaryBoard.get()) 2 else 1}"

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