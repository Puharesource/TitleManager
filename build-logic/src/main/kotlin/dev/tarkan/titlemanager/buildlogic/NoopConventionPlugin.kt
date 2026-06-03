package dev.tarkan.titlemanager.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class NoopConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}
