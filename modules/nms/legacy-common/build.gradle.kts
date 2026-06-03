plugins {
    kotlin("jvm")
    `java-library`
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()

val minecraftVersion = providers.gradleProperty("titleManagerPaperApiVersion").get()

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    api(project(":modules:bukkit:runtime-contracts"))
    api(project(":modules:bukkit:runtime-bukkit-api"))
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.mockbukkit)
    testImplementation("io.papermc.paper:paper-api:$minecraftVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
