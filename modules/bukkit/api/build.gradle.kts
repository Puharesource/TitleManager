import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm")
    `java-library`
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    `maven-publish`
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerJvmVersion").get()

base {
    archivesName.set("titlemanager-bukkit-api")
}
val minecraftVersion = providers.gradleProperty("titleManagerPaperApiVersion").get()

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    api(project(":modules:core"))
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion")
    testCompileOnly("io.papermc.paper:paper-api:$minecraftVersion")
    testRuntimeOnly("io.papermc.paper:paper-api:$minecraftVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.named("dokkaGeneratePublicationJavadoc"))
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/javadoc"))
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "titleManagerLocal"
            url = layout.buildDirectory.dir("titlemanager-local-maven").get().asFile.toURI()
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            artifactId = "titlemanager-bukkit-api"


            pom {
                name.set("TitleManager Bukkit API")
                description.set("Public Bukkit API for integrating with TitleManager.")
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
}
