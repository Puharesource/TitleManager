plugins {
    kotlin("jvm")
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
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testCompileOnly("io.papermc.paper:paper-api:$minecraftVersion")
    testRuntimeOnly("io.papermc.paper:paper-api:$minecraftVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
