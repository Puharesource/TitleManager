package dev.tarkan.titlemanager.lib.color

class Color(val red: ColorByte, val green: ColorByte, val blue: ColorByte) {
    constructor(red: Int, green: Int, blue: Int) : this(ColorByte(red.toUByte()), ColorByte(green.toUByte()), ColorByte(blue.toUByte()))
    constructor(red: ColorFloat, green: ColorFloat, blue: ColorFloat) : this(red.toColorByte(), green.toColorByte(), blue.toColorByte())
}
