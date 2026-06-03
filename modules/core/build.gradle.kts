import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("multiplatform")
    `maven-publish`
    id("org.jetbrains.dokka")
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerCoreVersion").get()

base {
    archivesName.set("titlemanager-core")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    js(IR) {
        binaries.library()
        generateTypeScriptDefinitions()
        nodejs()
        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_ES)
        }

        compilations["main"].packageJson {
            name = "@titlemanager/core"
            customField("description", "Portable TitleManager animation parser and timeline engine for Minecraft plugins and web previews.")
            customField("license", "MIT")
            customField("sideEffects", false)
            customField("type", "module")
            customField("keywords", listOf("titlemanager", "minecraft", "animation", "kotlin", "typescript"))
            customField(
                "repository",
                mapOf(
                    "type" to "git",
                    "url" to "https://github.com/Puharesource/TitleManager.git"
                )
            )
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")

            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            kotlin.srcDir("src/test/kotlin")
            resources.srcDir("src/test/resources")

            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }

    jvmToolchain(21)
}

val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/html"))
}

publishing {
    repositories {
        maven {
            name = "titleManagerLocal"
            url = layout.buildDirectory.dir("titlemanager-local-maven").get().asFile.toURI()
        }
    }

    publications.withType<MavenPublication>().configureEach {
        artifact(dokkaHtmlJar)

        artifactId = when (name) {
            "kotlinMultiplatform" -> "titlemanager-core"
            else -> "titlemanager-core-$name"
        }

        pom {
            name.set("TitleManager Core")
            description.set("Portable TitleManager animation parser and timeline engine for Minecraft plugins and web previews.")
            url.set("https://titlemanager.tarkan.dev")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/Puharesource/TitleManager/blob/main/LICENSE.md")
                }
            }
            developers {
                developer {
                    id.set("Puharesource")
                    name.set("Puharesource")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/Puharesource/TitleManager.git")
                developerConnection.set("scm:git:ssh://git@github.com/Puharesource/TitleManager.git")
                url.set("https://github.com/Puharesource/TitleManager")
            }
        }
    }
}

tasks.register("test") {
    dependsOn(tasks.named("allTests"))
}

val verifyJsPackageMetadata = tasks.register("verifyJsPackageMetadata") {
    dependsOn(tasks.named("jsNodeProductionLibraryDistribution"))

    val packageJsonFile = layout.buildDirectory.file("dist/js/productionLibrary/package.json")
    val typeDefinitionsFile = layout.buildDirectory.file("dist/js/productionLibrary/TitleManager-modules-core.d.mts")
    inputs.file(packageJsonFile)
    inputs.file(typeDefinitionsFile)

    doLast {
        val packageJson = packageJsonFile.get().asFile.readText()
        require("\"name\": \"@titlemanager/core\"" in packageJson) {
            "Generated JS package must publish as @titlemanager/core."
        }
        require("\"main\": \"TitleManager-modules-core.mjs\"" in packageJson) {
            "Generated JS package must expose the ES module build."
        }
        require("\"types\": \"TitleManager-modules-core.d.mts\"" in packageJson) {
            "Generated JS package must include TypeScript definitions."
        }
        require("\"type\": \"module\"" in packageJson) {
            "Generated JS package must be marked as an ES module package."
        }
        require("\"sideEffects\": false" in packageJson) {
            "Generated JS package should remain tree-shakeable for web viewers."
        }

        val typeDefinitions = typeDefinitionsFile.get().asFile.readText()
        require("export declare class TitleManagerTimelineFrame" in typeDefinitions) {
            "Generated JS package must expose timeline frame types for the web viewer."
        }
        require("createAnimationTimelineWithSafetyLimits" in typeDefinitions) {
            "Generated JS package must expose the safety-limited timeline API used by the web viewer."
        }
        require("renderLegacyText(text: string): Array<TitleManagerLegacyTextSegment>" in typeDefinitions) {
            "Generated JS package must expose legacy text rendering for browser previews."
        }
        require("splitTypedLineBreak(text: string, limit: number): Array<string>" in typeDefinitions) {
            "Generated JS package must expose legacy line-break splitting for command parity."
        }
        require("formatLegacyGradient(data: Nullable<string>): string" in typeDefinitions) {
            "Generated JS package must expose legacy gradient formatting for browser/Minecraft parity checks."
        }
        require("legacyColorCode(color: string): string" in typeDefinitions) {
            "Generated JS package must expose legacy color-code helpers for browser/Minecraft parity checks."
        }
    }
}

tasks.named("check") {
    dependsOn(verifyJsPackageMetadata)
}
