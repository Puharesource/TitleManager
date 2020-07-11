package io.puharesource.mc.titlemanager.internal.color

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import kotlin.math.floor

class ChatColorGradient {
    private val colors: List<ChatColor>
    private val continuous: Boolean

    constructor(vararg colors: ChatColor, continuous: Boolean = true) : this(colors.toList(), continuous)
    constructor(vararg colors: Color, continuous: Boolean = true) : this(colors.map { ChatColor.of(it) }, continuous)
    constructor(vararg hexColors: String, continuous: Boolean = true) : this(hexColors.map { ChatColor.of(it) }, continuous)

    constructor(colors: List<ChatColor>, continuous: Boolean = true) {
        this.colors = colors.toList()
        this.continuous = continuous

        if (this.colors.size < 2) {
            throw IllegalArgumentException("Invalid number of colors, Gradient must have at least two colors")
        }
    }

    fun getColorAt(percentage: Float): ChatColor {
        val stepSize = 1.0f / (colors.size - 1)
        val startColorIndex = floor(percentage / stepSize).toInt()

        val endColorIndex = calculateIndex(startColorIndex + 1)

        return ColorUtil.gradientColor(colors[startColorIndex], colors[endColorIndex], percentage % stepSize)
    }

    fun getColorAt(precision: Int, index: Int): ChatColor {
        val startColorIndex = floor(index / precision.toFloat()).toInt()
        val endColorIndex = calculateIndex(startColorIndex + 1)

        val precisionIndex = index % precision
        val percentage = precisionIndex / precision.toFloat()

        return ColorUtil.gradientColor(colors[startColorIndex], colors[endColorIndex], percentage)
    }

    fun isOutOfBounds(precision: Int, index: Int): Boolean {
        return index >= precision * (this.colors.size - 1)
    }

    private fun calculateIndex(index: Int): Int {
        if (continuous) {
            return index % colors.size
        }

        if (index < 0) {
            return 0
        }

        if (index >= colors.size) {
            return colors.size - 1
        }

        return index
    }
}
