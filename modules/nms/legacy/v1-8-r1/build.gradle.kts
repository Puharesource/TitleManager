plugins {
    kotlin("jvm")
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()

repositories {
    mavenCentral()

    maven {
        name = "elmakers"
        url = uri("https://maven.elmakers.com/repository/")
    }

    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(project(":modules:nms:legacy-common"))
    compileOnly("org.spigotmc:spigot:1.8-R0.1-SNAPSHOT")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
