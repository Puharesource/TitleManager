import org.bukkit.entity.Player
import java.lang.reflect.Proxy

val testPlayer = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) handler@ { _, method, _ ->
    if (method.name == "isOnline") {
        return@handler true
    }

    if (method.name == "getName") {
        return@handler "TestName"
    }

    if (method.name == "getDisplayName") {
        return@handler "Test"
    }

    if (method.name == "getPlayerListName") {
        return@handler "TestName"
    }

    if (method.name == "toString") {
        return@handler "TestPlayer#toString"
    }

    return@handler null
} as Player