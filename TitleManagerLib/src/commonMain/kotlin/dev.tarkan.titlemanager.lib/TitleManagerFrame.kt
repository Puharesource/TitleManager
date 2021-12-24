package dev.tarkan.titlemanager.lib

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@ExperimentalJsExport
data class TitleManagerFrame(val text: String, val timing: TitleManagerTiming)
