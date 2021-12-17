package io.puharesource.mc.titlemanager.api.v3

import dev.tarkan.titlemanager.lib.color.Color
import net.md_5.bungee.api.ChatColor

fun Color.toChatColor(): ChatColor = ChatColor.of(toJavaColor())
fun java.awt.Color.toTitleManagerColor() = Color(red, green, blue)
fun Color.toJavaColor() = java.awt.Color(red.value.toInt(), green.value.toInt(), blue.value.toInt())
