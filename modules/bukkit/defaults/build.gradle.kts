plugins {
    kotlin("jvm")
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()

kotlin {
    jvmToolchain(21)
}
