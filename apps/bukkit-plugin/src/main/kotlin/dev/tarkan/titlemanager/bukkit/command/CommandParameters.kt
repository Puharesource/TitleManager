package dev.tarkan.titlemanager.bukkit.command

import net.kyori.adventure.text.Component

import dev.tarkan.titlemanager.time.Timing
import java.util.TreeMap

class CommandParameters(
    parameters: Collection<CommandParameter>,
    val consumedArgumentCount: Int = parameters.size
) {
    companion object {
        private val PARAMETER_REGEX = """^-(?<parameterName>[\w-]+)(?:=(?<value>[^ ]+))?${'$'}""".toRegex()
        private val PARAMETER_DEFINITIONS = listOf(
            CommandParameterDefinition("silent", tooltip = "Do not send confirmation messages."),
            CommandParameterDefinition("world", tooltip = "Limit a broadcast to one world. Use -world=<name>."),
            CommandParameterDefinition("fadein", aliases = setOf("fade-in"), tooltip = "Title fade-in ticks. Use -fadein=<ticks>."),
            CommandParameterDefinition("stay", tooltip = "Title stay ticks. Use -stay=<ticks>."),
            CommandParameterDefinition("fadeout", aliases = setOf("fade-out"), tooltip = "Title fade-out ticks. Use -fadeout=<ticks>."),
            CommandParameterDefinition("radius", tooltip = "Limit a broadcast by radius. Use -radius=<blocks>.")
        )
        private val PARAMETER_NAMES = PARAMETER_DEFINITIONS.flatMapTo(mutableSetOf()) { it.names }
        private const val TICK_MILLISECONDS = 50u
        private val MAX_LEGACY_TITLE_TICKS = UInt.MAX_VALUE / TICK_MILLISECONDS

        fun fromArguments(args: Collection<String>): CommandParameters {
            val parameters = mutableListOf<CommandParameter>()

            for (argument in args) {
                val parameter = toParameterOrNull(argument) ?: break

                parameters += parameter
            }

            return CommandParameters(parameters, parameters.size)
        }

        fun isParameterArgument(input: String): Boolean {
            return PARAMETER_REGEX.matches(input)
        }

        fun completeParameter(prefix: String): List<String> {
            return completeParameterSuggestions(prefix).map { it.text }
        }

        fun completeParameterSuggestions(prefix: String): List<CommandSuggestion> {
            if (!prefix.startsWith("-")) {
                return emptyList()
            }

            return PARAMETER_DEFINITIONS
                .map { it.suggestion }
                .filter { it.text.startsWith(prefix, ignoreCase = true) }
        }

        private fun toParameterOrNull(input: String): CommandParameter? {
            val matchResult = PARAMETER_REGEX.matchEntire(input) ?: return null

            val parameterName = matchResult.groups["parameterName"]?.value ?: return null
            val value = matchResult.groups["value"]?.value

            return if (parameterName.lowercase() in PARAMETER_NAMES) CommandParameter(parameterName, value) else null
        }
    }

    private val parameters: Map<String, CommandParameter>

    val size: Int
        get() = parameters.size

    init {
        val parameterMap = TreeMap<String, CommandParameter>(String.CASE_INSENSITIVE_ORDER)

        parameterMap.putAll(parameters.associateBy { it.name })

        this.parameters = parameterMap
    }

    operator fun get(parameterName: String) = parameters[parameterName]

    operator fun contains(parameterName: String) = parameters.containsKey(parameterName)

    fun getValue(parameterName: String) = get(parameterName)?.value

    operator fun plus(other: CommandParameters): CommandParameters {
        return CommandParameters(parameters.values + other.parameters.values, consumedArgumentCount + other.consumedArgumentCount)
    }

    val isSilent: Boolean
        get() = "silent" in this

    fun toLegacyTitleTiming(): Timing {
        val fadeInMilliseconds = getTickMilliseconds("fadein", "fade-in", default = 20u, invalidFallback = 10u)
        val stayMilliseconds = getTickMilliseconds("stay", default = 20u, invalidFallback = 40u)
        val fadeOutMilliseconds = getTickMilliseconds("fadeout", "fade-out", default = 20u, invalidFallback = 10u)

        return Timing(fadeInMilliseconds, stayMilliseconds, fadeOutMilliseconds)
    }

    private fun getTickMilliseconds(vararg names: String, default: UInt, invalidFallback: UInt): UInt {
        val ticks = getTicks(*names, default = default, invalidFallback = invalidFallback)
        val safeTicks = if (ticks <= MAX_LEGACY_TITLE_TICKS) ticks else invalidFallback

        return safeTicks * TICK_MILLISECONDS
    }

    private fun getTicks(vararg names: String, default: UInt, invalidFallback: UInt): UInt {
        val parameter = names.firstNotNullOfOrNull { name -> get(name) } ?: return default

        return parameter.value?.toUIntOrNull() ?: invalidFallback
    }
}

private data class CommandParameterDefinition(
    val name: String,
    val aliases: Set<String> = emptySet(),
    val tooltip: String
) {
    val names: Set<String> = aliases + name
    val suggestion: CommandSuggestion = CommandSuggestion("-$name", Component.text(tooltip))
}
