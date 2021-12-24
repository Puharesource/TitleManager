import dev.tarkan.titlemanager.lib.sequences.CountDownSequence

fun main() {
    println("Hello there")

    CountDownSequence(5, false).sequence.forEach {
        println("internal: $it")
    }
}
