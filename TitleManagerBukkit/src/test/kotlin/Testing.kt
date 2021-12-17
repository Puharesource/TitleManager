import dev.tarkan.titlemanager.lib.color.HslColor
import dev.tarkan.titlemanager.lib.color.Interpolator
import dev.tarkan.titlemanager.lib.color.InterpolatorUtil
import io.puharesource.mc.titlemanager.api.v3.toJavaColor
import io.puharesource.mc.titlemanager.api.v3.toTitleManagerColor
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    testColors()
}

fun testColors() {
    val text = "My HEX color string"
    val interpolator = InterpolatorUtil.createHslGradientInterpolator(listOf(Color(255, 0, 0), Color(0, 255, 0), Color(0, 0, 255)).map { HslColor.fromColor(it.toTitleManagerColor()) })

    listOf(Color(255, 0, 0), Color(0, 255, 0), Color(0, 0, 255)).map { HslColor.fromColor(it.toTitleManagerColor()) }.map { it.toColor() }

    var squareText = ""
    for (i in text.indices) {
        squareText += "■"
    }

    createImage("C:\\Users\\pmtar\\test\\test.png", text, interpolator)
    createImage("C:\\Users\\pmtar\\test\\test_square.png", squareText, interpolator)
}

fun createImage(path: String, text: String, interpolator: Interpolator<HslColor>) {
    var bufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    var graphics = bufferedImage.createGraphics()
    val font = Font("JetBrains Mono", Font.PLAIN, 32)

    graphics.font = font

    var metrics = graphics.fontMetrics
    val width = metrics.stringWidth(text) + 5
    val height = metrics.height

    graphics.dispose()

    bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    graphics = bufferedImage.createGraphics()

    graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
    graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

    graphics.font = font

    metrics = graphics.fontMetrics

    for (i in text.indices) {
        val offsetIndex = i % text.length
        val percentage = offsetIndex.toFloat() / text.length.toFloat()

        val x = if (i == 0) 0 else metrics.stringWidth(text.substring(0, i))

        graphics.color = interpolator.interpolate(percentage).toColor().toJavaColor()
        graphics.drawString(text[i].toString(), x, metrics.ascent)
    }

    graphics.dispose()

    ImageIO.write(bufferedImage, "png", File(path))
}
