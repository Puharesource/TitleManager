
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import java.util.concurrent.ConcurrentSkipListMap

val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)\\](.*)$".toRegex()
val variablePattern = """[%][{]([^}]+\b)[}]""".toRegex()
val animationPattern = """[$][{]([^}]+\b)[}]""".toRegex()

val registeredAnimations : MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

fun main(args: Array<String>) {
    while (true) {
        val time = readLine()!!.toInt()

        var hours = time / 1000 + 6
        val minutes = time % 1000 * 60 / 1000
        var extra = ""

        val is24HourFormat = false

        if (is24HourFormat) {
            hours %= 24
        } else {
            hours %= 12

            if (hours == 0) {
                hours = 12
            }

            extra = " "

            if (time < 18000 && time >= 6000) {
                extra += "PM"
            } else {
                extra += "AM"
            }
        }

        println("${String.format("%02d", hours)}:${String.format("%02d", minutes)}$extra")
    }
}