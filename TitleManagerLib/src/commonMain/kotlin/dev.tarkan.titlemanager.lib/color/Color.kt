package dev.tarkan.titlemanager.lib.color

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@ExperimentalJsExport
class Color(val red: ColorByte, val green: ColorByte, val blue: ColorByte) {
    @JsName("fromInt")
    constructor(red: Int, green: Int, blue: Int) : this(ColorByte(red.toUByte()), ColorByte(green.toUByte()), ColorByte(blue.toUByte()))

    @JsName("fromFloat")
    constructor(red: ColorFloat, green: ColorFloat, blue: ColorFloat) : this(red.toColorByte(), green.toColorByte(), blue.toColorByte())
}
