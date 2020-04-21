import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    `maven-publish`

    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"

    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.10.0"
    id("net.saliman.properties") version "1.5.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

group = "io.puharesource.mc"
version = "2.2.2"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val fatJar by named("shadowJar", ShadowJar::class) {
        dependencies {
            include(dependency("org.jetbrains.kotlin:.*"))
            include(dependency("org.jetbrains.kotlinx:.*"))
            include(dependency("com.google.dagger:.*"))
            include(dependency("javax.inject:.*"))
        }

        relocate("kotlin", "io.puharesource.mc.titlemanager.shaded.kotlin")
        relocate("org.jetbrains", "io.puharesource.mc.titlemanager.shaded.org.jetbrains")
        relocate("dagger", "io.puharesource.mc.titlemanager.shaded.dagger")
        relocate("javax.inject", "io.puharesource.mc.titlemanager.shaded.javax.inject")
    }

    val dokka by getting(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/docs/javadoc/"

        configuration {
            jdkVersion = 8
            platform = "JVM"

            externalDocumentationLink {
                url = uri("https://hub.spigotmc.org/javadocs/spigot/").toURL()
                packageListUrl = uri("https://hub.spigotmc.org/javadocs/spigot/package-list").toURL()
            }

            sourceLink {
                path = "src/main/kotlin"
                url = "https://github.com/Puharesource/TitleManager/tree/master/src/main/kotlin"
                lineSuffix = "#L"
            }

            perPackageOption {
                prefix = "io.puharesource.mc.titlemanager.api.v2"
                suppress = false
            }

            perPackageOption {
                prefix = "io.puharesource.mc.titlemanager"
                suppress = true
            }
        }
    }

    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")

        dependsOn.add(dokka)
        from(dokka.outputDirectory)
    }

    val apiJar by creating(Jar::class) {
        archiveClassifier.set("api")

        from(sourceSets.main.get().output.classesDirs)
        include("io/puharesource/mc/titlemanager/api/v2/**")
    }

    val publishJar by creating(Jar::class) {
        archiveClassifier.set(null as String?)

        from(sourceSets.main.get().output.classesDirs)
        include("io/puharesource/mc/titlemanager/api/v2/**")
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")

        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        from(sourceSets.main.get().allSource)
    }

    artifacts {
        add("archives", fatJar)
        add("archives", apiJar)
        add("archives", javadocJar)
        add("archives", sourcesJar)
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Puharesource/TitleManager")

            credentials {
                username = (findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")) as? String
                password = (findProperty("gpr.key") ?: System.getenv("GPR_API_KEY")) as? String
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            pom {
                developers {
                    developer {
                        id.set("pmtn")
                        name.set("Tarkan Nielsen")
                        email.set("pmtarkan@gmail.com")
                        url.set("https://tarkan.dev")
                    }
                }
            }

            artifact(tasks["publishJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
        }
    }
}

(tasks.getByName("processResources") as ProcessResources).apply {
    from("src/main/resources") {
        include("**/*.yml")
        filter<ReplaceTokens>("tokens" to mapOf("VERSION" to project.version))
    }
    filesMatching("application.properties") {
        expand(project.properties)
    }
}

idea {
    module {
        generatedSourceDirs.add(file("$buildDir/generated/source/kapt"))
    }
}

repositories {
    jcenter()

    maven {
        name = "Spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }

    maven {
        name = "BungeeCord"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Vault"
        url = uri("http://nexus.hc.to/content/repositories/pub_releases")
    }

    maven {
        name = "ess-repo"
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        name = "placeholderapi"
        url = uri("http://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "mvdw-software"
        url = uri("http://repo.mvdw-software.com/content/groups/public/")
    }

    maven {
        name = "cubekrowd-repo"
        url = uri("https://mavenrepo.cubekrowd.net/artifactory/repo/")
    }

    maven {
        name = "kitteh-repo"
        url = uri("http://repo.kitteh.org/content/groups/public/")
    }

    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    implementation("com.google.dagger:dagger:2.27")
    kapt("com.google.dagger:dagger-compiler:2.27")

    implementation(group = "javax.inject", name = "javax.inject", version = "1")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.72")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.5")

    implementation(group = "org.spigotmc", name = "spigot-api", version = "1.14-R0.1-SNAPSHOT")

    implementation(group = "be.maximvdw", name = "MVdWPlaceholderAPI", version = "3.0.1-SNAPSHOT") { isTransitive = false }
    implementation(group = "me.clip", name = "placeholderapi", version = "2.10.4")
    implementation(group = "net.milkbowl.vault", name = "VaultAPI", version = "1.7") { isTransitive = false }
    implementation(group = "net.ess3", name = "EssentialsX", version = "2.17.1") { isTransitive = false }
    implementation(group = "de.myzelyam", name = "SuperVanish", version = "6.1.3") { isTransitive = false }
    implementation(group = "org.kitteh", name = "VanishNoPacket", version = "3.19.1") { isTransitive = false }
    implementation(group = "com.SirBlobman.combatlogx", name = "CombatLogX-API", version = "10.0.0.0-SNAPSHOT") { isTransitive = false }

    implementation(group = "org.graalvm.sdk", name = "graal-sdk", version = "19.2.0.1")

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit", version = "1.3.50")
}
