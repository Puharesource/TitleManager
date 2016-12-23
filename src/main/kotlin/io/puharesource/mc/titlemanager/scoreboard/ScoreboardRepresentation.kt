package io.puharesource.mc.titlemanager.scoreboard

import java.util.concurrent.ConcurrentHashMap

class ScoreboardRepresentation {
    var title : String
        set(value) {
            if (value.length > 32) {
                title = value.substring(0, 32)
            } else {
                title = value
            }
        }

    val lines : MutableMap<Int, String>

    val size : Int
        get() = lines.size

    constructor(title: String = "", lines: MutableMap<Int, String> = ConcurrentHashMap()) {
        this.title = title
        this.lines = lines
    }

    fun get(index: Int) = lines[index]
    fun set(index: Int, text: String) {
        if (text.length > 40) {
            lines[index] = text.substring(0, 40)
        } else {
            lines[index] = text
        }
    }
    fun remove(index: Int) = lines.remove(index)
}