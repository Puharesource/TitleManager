package io.puharesource.mc.titlemanager.internal.services.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import java.io.File

interface AnimationsService {
    val animations: Map<String, Animation>

    fun createAnimationsFolderIfNotExists()

    fun loadAnimations()

    fun createAnimationFromText(text: String): Animation
    fun createAnimationFromTextLines(vararg lines: String): Animation
    fun createAnimationFromTextFile(file: File): Animation

    fun addAnimation(name: String, animation: Animation)
    fun removeAnimation(name: String)

    fun containsAnimations(text: String): Boolean
    fun containsAnimation(text: String, animation: String): Boolean

    fun textToAnimationParts(text: String): List<AnimationPart<*>>
}
