package dev.tarkan.titlemanager.bukkit.metrics

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.AnnouncerConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.PlayerListConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.ScoreboardConfiguration
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie

class MetricsService(
    plugin: TitleManagerPlugin,
    advancedConfiguration: AdvancedConfiguration,
    playerListConfiguration: PlayerListConfiguration,
    scoreboardConfiguration: ScoreboardConfiguration,
    announcerConfiguration: AnnouncerConfiguration
) {
    val metrics: Metrics? = createMetrics(plugin)?.apply {
            addCustomChart(SimplePie("servers_using_config") { advancedConfiguration.usingConfig.toString() })
            addCustomChart(SimplePie("servers_using_player_list") { (advancedConfiguration.usingConfig && playerListConfiguration.enabled).toString() })
            addCustomChart(SimplePie("servers_using_scoreboard") { (advancedConfiguration.usingConfig && scoreboardConfiguration.enabled).toString() })
            addCustomChart(SimplePie("servers_using_bungeecord_features") { advancedConfiguration.usingBungeeCord.toString() })
            addCustomChart(SimplePie("servers_using_announcer") { (advancedConfiguration.usingConfig && announcerConfiguration.enabled).toString() })
        }

    val enabled: Boolean
        get() = metrics != null

    companion object {
        const val BSTATS_PLUGIN_ID = 7318

        private const val RELOCATION_ERROR = "bStats Metrics class has not been relocated correctly!"

        private fun createMetrics(plugin: TitleManagerPlugin): Metrics? {
            return try {
                Metrics(plugin, BSTATS_PLUGIN_ID)
            } catch (exception: IllegalStateException) {
                if (exception.message == RELOCATION_ERROR) {
                    plugin.logger.warning("bStats metrics are disabled for this unshaded runtime; the shadow jar relocates bStats for production.")
                    null
                } else {
                    throw exception
                }
            }
        }
    }
}
