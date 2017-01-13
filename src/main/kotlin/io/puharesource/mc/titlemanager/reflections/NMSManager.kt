package io.puharesource.mc.titlemanager.reflections

import org.bukkit.Bukkit
import java.util.TreeMap
import java.util.regex.Pattern

object NMSManager {
    private val VERSION_PATTERN : Pattern = Pattern.compile("(v|)[0-9][_.][0-9]+[_.][R0-9]*")
    private val supportedVersions : MutableMap<String, Int> = TreeMap(String.CASE_INSENSITIVE_ORDER)

    val serverVersion : String
    val versionIndex : Int

    init {
        supportedVersions.put("v1_7_R4", 0)
        supportedVersions.put("v1_8_R1", 1)
        supportedVersions.put("v1_8_R2", 2)
        supportedVersions.put("v1_8_R3", 2)
        supportedVersions.put("v1_9_R1", 2)
        supportedVersions.put("v1_9_R2", 2)
        supportedVersions.put("v1_10_R1", 3)
        supportedVersions.put("v1_11_R1", 4)

        val pkg: String = Bukkit.getServer().javaClass.`package`.name
        var version = pkg.substring(pkg.lastIndexOf(".") + 1)

        if (!VERSION_PATTERN.matcher(version).matches()) {
            version = ""
        }

        serverVersion = version
        versionIndex = getVersionIndex(version)
    }

    fun getVersionIndex(version: String) = supportedVersions.getOrElse(version) { 4 }

    fun getClassProvider() : NMSClassProvider {
        when (versionIndex) {
            0    -> return ProviderProtocolHack
            1    -> return Provider18
            2    -> return Provider183
            3    -> return Provider110
            4    -> return Provider110
            else -> return Provider110
        }
    }
}