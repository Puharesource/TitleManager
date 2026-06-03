package dev.tarkan.titlemanager.bukkit.context

import dev.tarkan.titlemanager.animation.Animation
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.PlayerListConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.ScoreboardConfiguration
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingMode
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.extensions.color
import dev.tarkan.titlemanager.bukkit.integration.ExternalPlaceholderIntegration
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.koin.java.KoinJavaComponent.inject
import java.io.Closeable
import java.time.Duration

@OptIn(FlowPreview::class)
data class PlayerContext(val player: Player, val plugin: TitleManagerPlugin) : Closeable {
    private val coroutineScopeManager: CoroutineScopeManager by inject(CoroutineScopeManager::class.java)
    private val intermediaryParser: IntermediaryParser by inject(IntermediaryParser::class.java)
    private val animationParser: AnimationParser<PlayerContext> by inject(AnimationParser::class.java)
    private val advancedConfiguration: AdvancedConfiguration by inject(AdvancedConfiguration::class.java)
    private val scoreboardConfiguration: ScoreboardConfiguration by inject(ScoreboardConfiguration::class.java)
    private val playerListConfiguration: PlayerListConfiguration by inject(PlayerListConfiguration::class.java)
    private val playerStorage: PlayerStorage by inject(PlayerStorage::class.java)
    private val runtimeVersionModule: RuntimeVersionModule by inject(RuntimeVersionModule::class.java)
    private val externalPlaceholderIntegration: ExternalPlaceholderIntegration by inject(ExternalPlaceholderIntegration::class.java)

    private var sidebar: RuntimeSidebar? = null
    private var playerListSessionId = 0L
    private var scoreboardSessionId = 0L
    private var titleSessionId = 0L
    private var subtitleSessionId = 0L
    private var actionbarSessionId = 0L
    private var lastPlayerListHeaderAndFooter: Pair<String, String>? = null
    private var lastScoreboardTitle: String? = null
    private val lastScoreboardValues = mutableMapOf<Int, String>()

    private var playerListJob: Job? = null
        set(value) {
            cancelPlayerListJob()

            field = value
        }

    private var scoreboardJob: Job? = null
        set(value) {
            field?.cancel()

            field = value
        }

    private var titleJob: Job? = null
        set(value) {
            cancelTitleJob()

            field = value
        }

    private var subtitleJob: Job? = null
        set(value) {
            cancelSubtitleJob()

            field = value
        }

    private var actionbarJob: Job? = null
        set(value) {
            cancelActionbarJob()

            field = value
        }

    fun hasScoreboard(): Boolean {
        return sidebar?.isAppliedTo(player) ?: false
    }

    fun requireSidebarCapability() {
        requireRuntimeCapability(RuntimeCapability.SIDEBAR)
    }

    fun requirePlayerListCapability() {
        requireRuntimeCapability(RuntimeCapability.PLAYER_LIST)
    }

    fun giveScoreboard() {
        requireSidebarCapability()

        if (!hasScoreboard()) {
            sidebar = callRuntimeBlocking(runtimeVersionModule.threadingPolicy.sidebar) {
                runtimeVersionModule.createSidebar(player)
            }
        }
    }

    fun removeScoreboard() {
        val activeSidebar = sidebar ?: return

        sidebar = null
        lastScoreboardTitle = null
        lastScoreboardValues.clear()
        callRuntimeBlocking(runtimeVersionModule.threadingPolicy.sidebar) {
            activeSidebar.close()
        }
    }

    fun getScoreboardTitle(): String? {
        return sidebar?.title
    }

    fun setScoreboardTitle(title: String) {
        requireSidebarCapability()

        callRuntimeBlocking(runtimeVersionModule.threadingPolicy.sidebar) {
            sidebar?.title = title
        }
    }

    fun getScoreboardValue(index: Int): String? {
        requireScoreboardIndex(index)

        return sidebar?.get(index)
    }

    fun setScoreboardValue(index: Int, value: String) {
        requireSidebarCapability()
        requireScoreboardIndex(index)

        callRuntimeBlocking(runtimeVersionModule.threadingPolicy.sidebar) {
            sidebar?.set(index, value)
        }
    }

    fun removeScoreboardValue(index: Int) {
        requireSidebarCapability()
        requireScoreboardIndex(index)

        callRuntimeBlocking(runtimeVersionModule.threadingPolicy.sidebar) {
            sidebar?.remove(index)
        }
    }

    fun sendTitle(title: String, timing: Timing = Timing.default, delay: Long = 0): Long {
        requireRuntimeCapability(RuntimeCapability.TITLES)

        val titleAnimation: Animation<PlayerContext, String> = createAnimation(title, timing)
        val sessionId = nextTitleSessionId()

        titleJob = coroutineScopeManager.scope.launch {
            var firstValue = true

            titleAnimation.flowWithTimings(this@PlayerContext)
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        delay
                    } else {
                        10L
                    }
                }
                .collect { (timings, title) ->
                    val stay = timings.totalMilliseconds.toInt() + 1000
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(stay.toLong()), Duration.ofMillis(0))

                    sendTitleTimes(times)
                    sendTitle(ComponentSerializer.deserialize(title.processExternalPlaceholders().color()))
                }
        }

        return sessionId
    }

    fun sendSubtitle(subtitle: String, timing: Timing = Timing.default, delay: Long = 0): Long {
        requireRuntimeCapability(RuntimeCapability.TITLES)

        val subtitleAnimation: Animation<PlayerContext, String> = createAnimation(subtitle, timing)
        val sessionId = nextSubtitleSessionId()

        subtitleJob = coroutineScopeManager.scope.launch {
            var firstValue = true

            subtitleAnimation.flowWithTimings(this@PlayerContext)
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        delay
                    } else {
                        10L
                    }
                }
                .collect { (timings, title) ->
                    val stay = timings.totalMilliseconds.toInt() + 1000
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(stay.toLong()), Duration.ofMillis(0))

                    sendTitleTimes(times)
                    sendSubtitle(ComponentSerializer.deserialize(title.processExternalPlaceholders().color()))
                }
        }

        return sessionId
    }

    fun sendTitleAndSubtitle(title: String, subtitle: String, timing: Timing, delay: Long = 0): Long {
        requireRuntimeCapability(RuntimeCapability.TITLES)

        val sessionId = nextTitleSessionId()
        subtitleSessionId = sessionId
        titleJob = coroutineScopeManager.scope.launch {
            val titleAnimation = createAnimation(title, timing)
            val subtitleAnimation = createAnimation(subtitle, timing)

            var firstValue = true

            titleAnimation.flowWithTimings(this@PlayerContext)
                .combine(subtitleAnimation.flowWithTimings(this@PlayerContext)) { titleFlow, subtitleFlow -> titleFlow to subtitleFlow }
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        delay
                    } else {
                        10L
                    }
                }
                .collect { (title, subtitle) ->
                    val stay = maxOf(title.timing.totalMilliseconds, subtitle.timing.totalMilliseconds).toInt() + 1000

                    showTitle(Title.title(
                        ComponentSerializer.deserialize(title.item.processExternalPlaceholders().color()),
                        ComponentSerializer.deserialize(subtitle.item.processExternalPlaceholders().color()),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(stay.toLong()), Duration.ofMillis(0))))
                }
        }

        subtitleJob = titleJob

        return sessionId
    }

    fun sendActionbar(message: String, delay: Long = 0): Long {
        requireRuntimeCapability(RuntimeCapability.ACTIONBAR)

        val animation: Animation<PlayerContext, String> = createAnimation(message)
        val sessionId = nextActionbarSessionId()

        actionbarJob = coroutineScopeManager.scope.launch {
            var firstValue = true

            animation.flow(this@PlayerContext)
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        delay
                    } else {
                        10L
                    }
                }
                .collect {
                    sendActionBar(ComponentSerializer.deserialize(it.processExternalPlaceholders().color()))
                }
        }

        return sessionId
    }

    fun setPlayerListHeaderAndFooter(
        header: String,
        footer: String,
        updateIntervalMilliseconds: Long = playerListConfiguration.updateIntervalMilliseconds
    ): Long {
        requirePlayerListCapability()

        val sessionId = nextPlayerListSessionId()

        playerListJob = coroutineScopeManager.scope.launch {
            val parsedHeaderValue = intermediaryParser.parseLine(header)
            val headerAnimation = animationParser.parseAnimation(parsedHeaderValue)

            val parsedFooterValue = intermediaryParser.parseLine(footer)
            val footerAnimation = animationParser.parseAnimation(parsedFooterValue)

            var firstValue = true

            headerAnimation.flow(this@PlayerContext, isInfinite = true)
                .combine(footerAnimation.flow(this@PlayerContext, isInfinite = true)) { headerFlow, footerFlow -> headerFlow to footerFlow }
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        0L
                    } else {
                        updateIntervalMilliseconds
                    }
                }
                .collect { (headerText, footerText) ->
                    val headerLine = headerText.processExternalPlaceholders().color()
                    val footerLine = footerText.processExternalPlaceholders().color()
                    val playerListState = headerLine to footerLine

                    if (advancedConfiguration.preventDuplicatePackets && playerListState == lastPlayerListHeaderAndFooter) {
                        return@collect
                    }

                    lastPlayerListHeaderAndFooter = playerListState

                    sendPlayerListHeaderAndFooter(
                        ComponentSerializer.deserialize(headerLine),
                        ComponentSerializer.deserialize(footerLine)
                    )
                }
        }

        return sessionId
    }

    fun setScoreboard(
        title: String,
        content: List<String>,
        updateIntervalMilliseconds: Long = scoreboardConfiguration.updateIntervalMilliseconds
    ): Long {
        requireSidebarCapability()
        require(content.size <= MAX_SCOREBOARD_LINES) {
            "Sidebars support at most $MAX_SCOREBOARD_LINES lines. Lines provided: ${content.size}"
        }
        require(updateIntervalMilliseconds > 0) { "Scoreboard update interval must be greater than zero milliseconds" }

        val sessionId = nextScoreboardSessionId()
        cancelScoreboardJob()
        giveScoreboard()

        scoreboardJob = coroutineScopeManager.scope.launch {
            for ((index, line) in content.withIndex()) {
                ensureActive()

                this.launch {
                    val parsedLineValue = intermediaryParser.parseLine(line)
                    val lineAnimation = animationParser.parseAnimation(parsedLineValue)
                    var firstValue = true

                    lineAnimation.flow(this@PlayerContext, isInfinite = true)
                        .debounce {
                            if (firstValue) {
                                firstValue = false
                                0L
                            } else {
                                updateIntervalMilliseconds
                            }
                        }
                        .collect { lineText ->
                            setScoreboardValueAsync(index + 1, lineText.processExternalPlaceholders().color())
                        }
                }
            }

            val parsedTitleValue = intermediaryParser.parseLine(title)
            val titleAnimation = animationParser.parseAnimation(parsedTitleValue)
            var firstValue = true

            titleAnimation.flow(this@PlayerContext, isInfinite = true)
                .debounce {
                    if (firstValue) {
                        firstValue = false
                        0L
                    } else {
                        updateIntervalMilliseconds
                    }
                }
                .collect { titleText ->
                    setScoreboardTitleAsync(titleText.processExternalPlaceholders().color())
                }
        }

        return sessionId
    }

    fun cancelPlayerListJob() {
        val isActive = playerListJob?.isActive ?: false

        playerListJob?.cancel()

        if (isActive && player.isOnline) {
            callRuntimeBlocking(runtimeVersionModule.threadingPolicy.playerList) {
                runtimeVersionModule.sendPlayerListHeaderAndFooter(player, Component.empty(), Component.empty())
            }
        }
        lastPlayerListHeaderAndFooter = null
    }

    fun cancelTitleJob() {
        val hadJob = titleJob != null
        val shouldClearSubtitle = subtitleJob == titleJob

        titleJob?.cancel()

        if (hadJob && player.isOnline) {
            callRuntimeBlocking(runtimeVersionModule.threadingPolicy.title) {
                runtimeVersionModule.sendTitle(player, Component.empty())
            }
            if (shouldClearSubtitle) {
                callRuntimeBlocking(runtimeVersionModule.threadingPolicy.title) {
                    runtimeVersionModule.sendSubtitle(player, Component.empty())
                }
            }
        }
    }

    fun cancelSubtitleJob() {
        val hadJob = subtitleJob != null

        subtitleJob?.cancel()

        if (hadJob && player.isOnline) {
            callRuntimeBlocking(runtimeVersionModule.threadingPolicy.title) {
                runtimeVersionModule.sendSubtitle(player, Component.empty())
            }
        }
    }

    fun cancelScoreboardJob() {
        scoreboardJob?.cancel()

        if (sidebar != null && player.isOnline) {
            removeScoreboard()
        }
        lastScoreboardTitle = null
        lastScoreboardValues.clear()
    }

    fun cancelActionbarJob() {
        val hadJob = actionbarJob != null

        actionbarJob?.cancel()

        if (hadJob && player.isOnline) {
            callRuntimeBlocking(runtimeVersionModule.threadingPolicy.actionbar) {
                runtimeVersionModule.sendActionBar(player, Component.empty())
            }
        }
    }

    fun cancelTitleSession(sessionId: Long) {
        if (titleSessionId == sessionId) {
            cancelTitleJob()
        }
    }

    fun cancelActionbarSession(sessionId: Long) {
        if (actionbarSessionId == sessionId) {
            cancelActionbarJob()
        }
    }

    fun cancelPlayerListSession(sessionId: Long) {
        if (playerListSessionId == sessionId) {
            cancelPlayerListJob()
        }
    }

    fun cancelScoreboardSession(sessionId: Long) {
        if (scoreboardSessionId == sessionId) {
            cancelScoreboardJob()
        }
    }

    override fun close() {
        cancelPlayerListJob()
        val subtitleSharesTitleJob = subtitleJob === titleJob
        cancelTitleJob()
        if (!subtitleSharesTitleJob) {
            cancelSubtitleJob()
        }
        cancelActionbarJob()
        cancelScoreboardJob()
    }

    fun setConfigScoreboard() {
        val config = scoreboardConfiguration.worlds.getOrDefault(player.world.name, scoreboardConfiguration)
        val playerInfo = playerStorage.get(player)

        cancelScoreboardJob()

        if (!config.enabled || !playerInfo.isSidebarEnabled) {
            return
        }

        setScoreboard(config.title, config.content.split('\n'), config.updateIntervalMilliseconds)
    }

    fun setConfigPlayerList() {
        val config = playerListConfiguration.worlds.getOrDefault(player.world.name, playerListConfiguration)
        val playerInfo = playerStorage.get(player)

        if (!config.enabled || !playerInfo.isPlayerListEnabled) {
            cancelPlayerListJob()

            return
        }

        setPlayerListHeaderAndFooter(config.header, config.footer, config.updateIntervalMilliseconds)
    }

    private fun createAnimation(line: String, timing: Timing = Timing.default): Animation<PlayerContext, String> {
        val parsedLine = intermediaryParser.parseLine(line, timing)

        return animationParser.parseAnimation(parsedLine)
    }

    private fun String.processExternalPlaceholders(): String {
        return externalPlaceholderIntegration.replace(player, this)
    }

    private companion object {
        const val MAX_SCOREBOARD_LINES = 15
    }

    private suspend fun sendTitleTimes(times: Title.Times) {
        callRuntime(runtimeVersionModule.threadingPolicy.title) {
            runtimeVersionModule.sendTitleTimes(player, times)
        }
    }

    private suspend fun sendTitle(title: Component) {
        callRuntime(runtimeVersionModule.threadingPolicy.title) {
            runtimeVersionModule.sendTitle(player, title)
        }
    }

    private suspend fun sendSubtitle(subtitle: Component) {
        callRuntime(runtimeVersionModule.threadingPolicy.title) {
            runtimeVersionModule.sendSubtitle(player, subtitle)
        }
    }

    private suspend fun showTitle(title: Title) {
        callRuntime(runtimeVersionModule.threadingPolicy.title) {
            runtimeVersionModule.showTitle(player, title)
        }
    }

    private suspend fun sendActionBar(actionBar: Component) {
        callRuntime(runtimeVersionModule.threadingPolicy.actionbar) {
            runtimeVersionModule.sendActionBar(player, actionBar)
        }
    }

    private suspend fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
        callRuntime(runtimeVersionModule.threadingPolicy.playerList) {
            runtimeVersionModule.sendPlayerListHeaderAndFooter(player, header, footer)
        }
    }

    private suspend fun setScoreboardTitleAsync(title: String) {
        if (advancedConfiguration.preventDuplicatePackets && title == lastScoreboardTitle) {
            return
        }
        lastScoreboardTitle = title

        callRuntime(runtimeVersionModule.threadingPolicy.sidebar) {
            sidebar?.title = title
        }
    }

    private suspend fun setScoreboardValueAsync(index: Int, value: String) {
        requireScoreboardIndex(index)

        if (advancedConfiguration.preventDuplicatePackets && lastScoreboardValues[index] == value) {
            return
        }
        lastScoreboardValues[index] = value

        callRuntime(runtimeVersionModule.threadingPolicy.sidebar) {
            sidebar?.set(index, value)
        }
    }

    private fun requireScoreboardIndex(index: Int) {
        require(index in 1..MAX_SCOREBOARD_LINES) {
            "Index needs to be in the range of 1 to $MAX_SCOREBOARD_LINES (1 and $MAX_SCOREBOARD_LINES inclusive). Index provided: $index"
        }
    }

    private fun requireRuntimeCapability(capability: String) {
        val status = runtimeVersionModule.capabilities.singleOrNull { it.name == capability }
        if (status?.status == RuntimeCapabilityStatus.AVAILABLE) {
            return
        }

        val statusDescription = status?.let { "${it.status} (${it.detail})" } ?: "missing"
        throw UnsupportedOperationException(
            "Runtime module '${runtimeVersionModule.displayName}' does not support capability '$capability': $statusDescription"
        )
    }

    private suspend fun <T> callRuntime(threadingMode: String, operation: () -> T): T {
        if (threadingMode != RuntimeThreadingMode.MAIN_THREAD || Bukkit.isPrimaryThread()) {
            return operation()
        }

        return withContext(coroutineScopeManager.syncContext) {
            operation()
        }
    }

    private fun <T> callRuntimeBlocking(threadingMode: String, operation: () -> T): T {
        if (threadingMode != RuntimeThreadingMode.MAIN_THREAD || Bukkit.isPrimaryThread()) {
            return operation()
        }

        return runBlocking {
            withContext(coroutineScopeManager.syncContext) {
                operation()
            }
        }
    }

    private fun nextTitleSessionId(): Long {
        titleSessionId += 1
        return titleSessionId
    }

    private fun nextSubtitleSessionId(): Long {
        subtitleSessionId += 1
        return subtitleSessionId
    }

    private fun nextActionbarSessionId(): Long {
        actionbarSessionId += 1
        return actionbarSessionId
    }

    private fun nextPlayerListSessionId(): Long {
        playerListSessionId += 1
        return playerListSessionId
    }

    private fun nextScoreboardSessionId(): Long {
        scoreboardSessionId += 1
        return scoreboardSessionId
    }
}
