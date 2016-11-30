
import com.google.common.base.Joiner
import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.animations.EasySendableAnimation
import io.puharesource.mc.titlemanager.animations.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import javafx.application.Application
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import net.md_5.bungee.api.chat.TextComponent
import netscape.javascript.JSObject
import org.bukkit.entity.Player
import java.io.File
import java.lang.reflect.Proxy

fun main(args: Array<String>) {
    Application.launch(TestTextDisplay::class.java, *args)
}

fun relativeFile(url: String) = File(url.replace("/", File.separator))
fun relativeFileURL(url: String) = relativeFile(url).toURI().toURL().toString()

val testPlayer = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java), handler@ { proxy, method, args ->
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
}) as Player

class TestTextDisplay : Application() {
    override fun start(stage: Stage) {
        stage.title = "TitleManager Test Display"
        stage.width = 800.0
        stage.height = 200.0

        stage.scene = Scene(Browser, 800.0, 600.0, Color.WHITE)
        stage.show()
    }
}

object Browser : Region() {
    val browser = WebView()
    val engine : WebEngine = browser.engine

    var currentAnimation : SendableAnimation? = null

    init {
        browser.isContextMenuEnabled = false

        engine.load(relativeFileURL("src/test/resources/test.html"))
        children.add(browser)

        // Process page loading
        engine.loadWorker.stateProperty().addListener { value, oldState, newState ->
            if (newState != Worker.State.SUCCEEDED) return@addListener

            val window = engine.executeScript("window") as JSObject
            window.setMember("jcallback", JavaCallback)

            window.call("onFinishedLoading")
            setAnimation("§b\${marquee:[5]Super swag dude! }")
        }
    }

    fun setText(formattedText: String) {
        val parts = TextComponent.fromLegacyText(formattedText)
                .map {
                    val text = it.toPlainText()
                    val classes = mutableListOf("mc-format-${it.color.toString().substring(1)}")

                    if (it.isBold) {
                        classes.add("mc-format-l")
                    }

                    if (it.isItalic) {
                        classes.add("mc-format-o")
                    }

                    if (it.isUnderlined) {
                        classes.add("mc-format-n")
                    }

                    if (it.isStrikethrough) {
                        classes.add("mc-format-m")
                    }

                    if (it.isObfuscated) {
                        classes.add("mc-format-k")
                    }

                    "<span class=\"${Joiner.on(' ').join(classes)}\">$text</span>"
                }

        val html = Joiner.on("").join(parts)

        Platform.runLater { engine.executeScript("setText('$html');") }
    }

    fun setAnimation(animation: Animation) {
        currentAnimation?.stop()

        currentAnimation = EasySendableAnimation(animation, testPlayer, {
            setText(it.text)
        }, continuous = true)

        currentAnimation?.start()
    }

    fun setAnimation(parts: List<AnimationPart<*>>) {
        currentAnimation?.stop()

        currentAnimation = PartBasedSendableAnimation(parts, testPlayer, {
            setText(it.text)
        }, continuous = true)

        currentAnimation?.start()
    }

    fun setAnimation(text: String) = setAnimation(APIProvider.toAnimationParts(text))

    override fun layoutChildren() = layoutInArea(browser, 0.0, 0.0, width, height, 0.0, HPos.CENTER, VPos.CENTER)
}

object JavaCallback {
    fun log(text: String) = println(text)
}