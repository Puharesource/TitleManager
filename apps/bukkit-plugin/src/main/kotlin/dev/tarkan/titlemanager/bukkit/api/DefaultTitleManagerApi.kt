package dev.tarkan.titlemanager.bukkit.api

import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.time.Timing
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class DefaultTitleManagerApi(
    private val playerContextManager: PlayerContextManager
) : TitleManagerApi {
    override fun showTitle(player: Player, title: String, subtitle: String, timing: Timing): TitleManagerSession {
        val context = playerContextManager.getContext(player)
        val sessionId = context.sendTitleAndSubtitle(title, subtitle, timing)

        return DefaultTitleManagerSession(player.uniqueId, TitleManagerSessionType.TITLE) {
            context.cancelTitleSession(sessionId)
        }
    }

    override fun sendActionbar(player: Player, message: String): TitleManagerSession {
        val context = playerContextManager.getContext(player)
        val sessionId = context.sendActionbar(message)

        return DefaultTitleManagerSession(player.uniqueId, TitleManagerSessionType.ACTIONBAR) {
            context.cancelActionbarSession(sessionId)
        }
    }

    override fun setPlayerListHeaderAndFooter(player: Player, header: String, footer: String): TitleManagerSession {
        val context = playerContextManager.getContext(player)
        val sessionId = context.setPlayerListHeaderAndFooter(header, footer)

        return DefaultTitleManagerSession(player.uniqueId, TitleManagerSessionType.PLAYER_LIST) {
            context.cancelPlayerListSession(sessionId)
        }
    }

    override fun setSidebar(player: Player, title: String, lines: List<String>): TitleManagerSession {
        val context = playerContextManager.getContext(player)
        val sessionId = context.setScoreboard(title, lines)

        return DefaultTitleManagerSession(player.uniqueId, TitleManagerSessionType.SIDEBAR) {
            context.cancelScoreboardSession(sessionId)
        }
    }

    override fun clearTitle(player: Player) {
        val context = playerContextManager.getContext(player)

        context.cancelTitleJob()
        context.cancelSubtitleJob()
    }

    override fun clearActionbar(player: Player) {
        playerContextManager.getContext(player).cancelActionbarJob()
    }

    override fun clearPlayerListHeaderAndFooter(player: Player) {
        playerContextManager.getContext(player).cancelPlayerListJob()
    }

    override fun clearSidebar(player: Player) {
        playerContextManager.getContext(player).cancelScoreboardJob()
    }

    private class DefaultTitleManagerSession(
        override val playerUniqueId: UUID,
        override val type: TitleManagerSessionType,
        private val closeSession: () -> Unit
    ) : TitleManagerSession {
        private val closed = AtomicBoolean(false)
        override val isClosed: Boolean
            get() = closed.get()

        override fun close() {
            if (closed.compareAndSet(false, true)) {
                closeSession()
            }
        }
    }
}
