
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import java.util.concurrent.ConcurrentSkipListMap

val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)\\](.*)$".toRegex()
val variablePattern = """[%][{]([^}]+\b)[}]""".toRegex()
val animationPattern = """[$][{]([^}]+\b)[}]""".toRegex()

val registeredAnimations : MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

fun main(args: Array<String>) {
    val testString = "\${left-to-right}The quick brown \${right-to-left}fox jumps over the lazy dog\${left-to-right}"
}