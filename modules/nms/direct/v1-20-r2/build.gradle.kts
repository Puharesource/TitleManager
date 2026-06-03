plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()

val minecraftVersion = "1.20.2-R0.1-SNAPSHOT"

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":modules:bukkit:runtime-contracts"))
    implementation(project(":modules:bukkit:runtime-bukkit-api"))
    implementation(project(":modules:nms:common"))
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
    paperweight.paperDevBundle(minecraftVersion)

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
