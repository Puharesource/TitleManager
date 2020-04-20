package io.puharesource.mc.titlemanager.internal.extensions

import org.bukkit.World

fun World.getFormattedTime(is24HourFormat: Boolean = false): String {
    var hours = time.toInt() / 1000 + 6
    val minutes = time.toInt() % 1000 * 60 / 1000
    var extra = ""

    if (is24HourFormat) {
        hours %= 24
    } else {
        hours %= 12

        if (hours == 0) {
            hours = 12
        }

        extra = " "

        extra += if (time in 7000..18999) {
            "PM"
        } else {
            "AM"
        }
    }

    return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}$extra"
}
