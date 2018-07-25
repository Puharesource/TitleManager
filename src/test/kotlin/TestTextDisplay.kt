
import com.google.common.base.Joiner
import com.madgag.gif.fmsware.AnimatedGifEncoder
import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.animations.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.extensions.color
import javafx.application.Application
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import net.md_5.bungee.api.chat.TextComponent
import netscape.javascript.JSObject
import org.bukkit.entity.Player
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Proxy
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main(args: Array<String>) = Application.launch(TestTextDisplay::class.java, *args)

fun relativeFile(url: String) = File(url.replace("/", File.separator))
fun relativeFileURL(url: String) = relativeFile(url).toURI().toURL().toString()

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

class TestTextDisplay : Application() {
    override fun start(stage: Stage) {
        stage.title = "TitleManager Test Display"
        stage.width = 800.0
        stage.height = 210.0
        stage.isResizable = false
        stage.setOnCloseRequest {
            Platform.exit()
            System.exit(0)
        }

        // Components
        val animationText = TextField("&b\${marquee:[0;3;0]This is my test text! }".color())
        val animationButton = Button("Set Animation")
        val gifAnimationButton = Button("Gif Animation")
        val buttonLayout = HBox(10.0)
        val layout = VBox(10.0)
        val font = Font.loadFont(TestTextDisplay::class.java.getResourceAsStream("minecraftfont.ttf"), 14.0)

        // Add buttons to button layout
        buttonLayout.children.addAll(animationButton, gifAnimationButton)
        buttonLayout.alignment = Pos.CENTER

        // Set layout
        layout.style = "-fx-padding: 10; -fx-background-color: #262626;"
        layout.children.addAll(Browser, animationText, buttonLayout)
        layout.alignment = Pos.CENTER

        // Set text field layout
        animationText.style = "-fx-border-color: white; -fx-text-fill: white;"
        animationText.font = font
        animationText.background = Background.EMPTY
        animationText.alignment = Pos.CENTER

        // Set animation button layout
        animationButton.style = "-fx-border-color: white; -fx-text-fill: white;"
        animationButton.font = font
        animationButton.background = Background.EMPTY

        // Set gif button layout
        gifAnimationButton.style = "-fx-border-color: white; -fx-text-fill: white;"
        gifAnimationButton.font = font
        gifAnimationButton.background = Background.EMPTY

        // Set animation button listener
        animationButton.setOnAction {
            Browser.setAnimation(animationText.text.color())
        }

        // Set gif button listener
        gifAnimationButton.setOnAction {
            Browser.setAnimation(animationText.text.color(), true)
        }

        stage.scene = Scene(layout, 800.0, 200.0, Color.DARKGRAY)
        stage.show()
    }
}

object Browser : Region() {
    val browser = WebView()
    val engine : WebEngine = browser.engine

    var currentAnimation : SendableAnimation? = null
    var images: MutableList<BufferedImage> = CopyOnWriteArrayList()

    init {
        browser.isContextMenuEnabled = false

        browser.setPrefSize(800.0, 80.0)

        engine.load(relativeFileURL("src/test/resources/test.html"))
        children.add(browser)

        // Process page loading
        engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState != Worker.State.SUCCEEDED) return@addListener

            val window = engine.executeScript("window") as JSObject
            window.setMember("jcallback", JavaCallback)

            window.call("onFinishedLoading")
        }
    }

    fun captureImage() : BufferedImage {
        val image = browser.snapshot(null, null)

        return SwingFXUtils.fromFXImage(image, null)
    }

    fun captureImage(file: File) {
        ImageIO.write(captureImage(), "png", file)

        println("Saved image to: " + file.absoluteFile)
    }

    fun setText(formattedText: String) {
        val parts = TextComponent.fromLegacyText(formattedText)
                .map {
                    val text = it.toPlainText()
                    val classes = mutableListOf("mc-format-${it.color.toString().substring(1)}")
                    fun add(char: Char, shouldAdd: Boolean) {
                        if (shouldAdd) {
                            classes.add("mc-format-$char")
                        }
                    }

                    add('l', it.isBold)
                    add('o', it.isItalic)
                    add('n', it.isUnderlined)
                    add('m', it.isStrikethrough)
                    add('k', it.isObfuscated)

                    "<span class=\"${Joiner.on(' ').join(classes)}\">$text</span>"
                }

        val html = Joiner.on("").join(parts)

        Platform.runLater { engine.executeScript("setText('$html');") }
    }

    fun setAnimation(parts: List<AnimationPart<*>>, capture: Boolean = false) {
        currentAnimation?.stop()

        if (capture) {
            println("Starting GIF generation")
            var firstFrame = true
            var lastFrameText : String? = null
            var differentFrames = 0

            currentAnimation = PartBasedSendableAnimation(parts, testPlayer, { frame ->
                if (firstFrame) {
                    firstFrame = false
                } else {
                    Platform.runLater { images.add(captureImage()) }
                }

                if (frame.text != lastFrameText) {
                    lastFrameText = frame.text
                    differentFrames++
                }

                setText(frame.text)
            }, onStop = Runnable {
                Platform.runLater {
                    images.add(captureImage())
                    // Create file
                    println("Creating gif file")
                    val gifFile = File("capture.gif")
                    if (gifFile.exists()) {
                        gifFile.delete()
                    }
                    gifFile.createNewFile()

                    // Create gif encoder
                    println("Creating GIF generator")

                    val gif = AnimatedGifEncoder()
                    gif.setRepeat(0)
                    gif.setDelay(50)
                    gif.setFrameRate(20f)

                    println("Start gif creation")
                    gif.start("capture.gif")

                    images.forEachIndexed { i, image ->
                        if (i != images.size - 1) {
                            gif.addFrame(image)
                        } else {
                            (1..(images.size / differentFrames)).forEach {
                                gif.addFrame(image)
                            }
                        }

                        println("Added image (${i + 1} / ${images.size})")
                    }

                    gif.finish()
                    images.clear()

                    println("GIF generation done")
                    setText("GIF saved")
                }
            })
        } else {
            currentAnimation = PartBasedSendableAnimation(parts, testPlayer, {
                setText(it.text)
            }, continuous = true)
        }

        currentAnimation?.start()
    }

    fun setAnimation(text: String, capture: Boolean = false) = setAnimation(APIProvider.toAnimationParts(text), capture = capture)

    val delayExecutor : ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    fun delay(body: () -> Unit) : ScheduledFuture<*> = delayExecutor.schedule({ Platform.runLater(body) }, 25, TimeUnit.MILLISECONDS)

    override fun layoutChildren() = layoutInArea(browser, 0.0, 0.0, width, height, 0.0, HPos.CENTER, VPos.CENTER)
}

object JavaCallback {
    fun log(text: String) = println(text)
}