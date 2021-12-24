plugins {
    kotlin("multiplatform") version "1.6.10"
}

group = "dev.tarkan.titlemanager.lib"
version = "3.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "titlemanager.js"
                output.libraryTarget = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.WINDOW
            }
        }

        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

tasks.create("copyJsToWeb") {
    shouldRunAfter("jsBrowserDistribution")

    copy {
        from(
            "build/distributions/titlemanager.js",
            "build/distributions/titlemanager.js.map",
            "$rootDir/build/packages/TitleManager-TitleManagerLib/kotlin/TitleManager-TitleManagerLib.d.ts"
        )

        into("../TitleManagerWeb/generated/")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += arrayOf("-opt-in=kotlin.RequiresOptIn", "-Xir-per-module")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().all {
    kotlinOptions.freeCompilerArgs += arrayOf("-opt-in=kotlin.RequiresOptIn", "-Xir-per-module")

    dependsOn("copyJsToWeb")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon>().all {
    kotlinOptions.freeCompilerArgs += arrayOf("-opt-in=kotlin.RequiresOptIn", "-Xir-per-module")
}
