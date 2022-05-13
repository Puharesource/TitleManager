package io.puharesource.mc.titlemanager.internal.reflections

import org.bukkit.Bukkit
import java.util.TreeMap
import java.util.regex.Pattern

object NMSManager {
    private val VERSION_PATTERN: Pattern = Pattern.compile("(v|)[0-9][_.][0-9]+[_.][R0-9]*")
    private val supportedVersions: MutableMap<String, Int> = TreeMap(String.CASE_INSENSITIVE_ORDER)

    val serverVersion: String
    val versionIndex: Int

    init {
        supportedVersions["v1_7_R4"] = 0
        supportedVersions["v1_8_R1"] = 1
        supportedVersions["v1_8_R2"] = 2
        supportedVersions["v1_8_R3"] = 2
        supportedVersions["v1_9_R1"] = 3
        supportedVersions["v1_9_R2"] = 3
        supportedVersions["v1_10_R1"] = 4
        supportedVersions["v1_11_R1"] = 5
        supportedVersions["v1_12_R1"] = 6
        supportedVersions["v1_13_R1"] = 7
        supportedVersions["v1_13_R2"] = 8
        supportedVersions["v1_14_R1"] = 9
        supportedVersions["v1_15_R1"] = 9
        supportedVersions["v1_16_R1"] = 10
        supportedVersions["v1_17_R1"] = 11
        supportedVersions["v1_18_R1"] = 12
        supportedVersions["v1_18_R2"] = 13

        val pkg: String = Bukkit.getServer().javaClass.`package`.name
        var version = pkg.substring(pkg.lastIndexOf(".") + 1)

        if (!VERSION_PATTERN.matcher(version).matches()) {
            version = ""
        }

        serverVersion = version
        versionIndex = getVersionIndex(version)
    }

    private fun getVersionIndex(version: String) = supportedVersions.getOrElse(version) {
        supportedVersions.values.maxOrNull() ?: -1
    }

    fun getClassProvider(): NMSClassProvider {
        return when (versionIndex) {
            0 -> ProviderProtocolHack
            1 -> Provider18
            2 -> Provider183
            3 -> Provider183
            4 -> Provider110
            5 -> Provider110
            6 -> Provider112
            7 -> Provider113
            8 -> Provider113
            9 -> Provider113
            10 -> Provider116
            11 -> Provider117
            12 -> Provider117
            else -> Provider117
        }
    }
}
