plugins {
    java
    idea
    `maven-publish`

    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"

    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("org.jetbrains.dokka") version "1.6.0"
    id("net.saliman.properties") version "1.5.1"
    id("org.jmailen.kotlinter") version "3.7.0"
}

subprojects {
    group = "io.puharesource.mc"
}

group = "io.puharesource.mc"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlinter {
    ignoreFailures = false
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = false
    disabledRules = arrayOf("import-ordering")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    val fatJar by named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        dependencies {
            include(dependency("org.jetbrains.kotlin:.*"))
            include(dependency("org.jetbrains.kotlinx:.*"))
            include(dependency("com.google.dagger:.*"))
            include(dependency("javax.inject:.*"))
            include(dependency("org.bstats:.*"))

            for (sub in subprojects) {
                include(dependency(sub))
            }
        }

        relocate("kotlin", "io.puharesource.mc.titlemanager.shaded.kotlin")
        relocate("org.jetbrains", "io.puharesource.mc.titlemanager.shaded.org.jetbrains")
        relocate("dagger", "io.puharesource.mc.titlemanager.shaded.dagger")
        relocate("javax.inject", "io.puharesource.mc.titlemanager.shaded.javax.inject")
        relocate("org.bstats", "io.puharesource.mc.titlemanager.shaded.org.bstats")
    }

    dokkaHtml {
        outputDirectory.set(buildDir.resolve("docs"))

        dokkaSourceSets {
            configureEach {
                jdkVersion.set(8)

                includeNonPublic.set(false)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)

                platform.set(org.jetbrains.dokka.Platform.jvm)

                sourceRoot(file("src/main/kotlin"))

                externalDocumentationLink {
                    url.set(uri("https://hub.spigotmc.org/javadocs/spigot/").toURL())
                    packageListUrl.set(uri("https://hub.spigotmc.org/javadocs/spigot/element-list").toURL())
                }

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(uri("https://github.com/Puharesource/TitleManager/tree/master/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }

                perPackageOption {
                    matchingRegex.set("io.puharesource.mc.titlemanager.api.v2.*")
                    suppress.set(false)
                }

                perPackageOption {
                    matchingRegex.set("io.puharesource.mc.titlemanager.*")
                    suppress.set(true)
                }
            }
        }
    }

    dokkaJavadoc {
        outputDirectory.set(buildDir.resolve("docs/javadoc"))

        dokkaSourceSets {
            configureEach {
                jdkVersion.set(8)

                includeNonPublic.set(false)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)

                platform.set(org.jetbrains.dokka.Platform.jvm)

                sourceRoot(file("src/main/kotlin"))

                externalDocumentationLink {
                    url.set(uri("https://hub.spigotmc.org/javadocs/spigot/").toURL())
                    packageListUrl.set(uri("https://hub.spigotmc.org/javadocs/spigot/element-list").toURL())
                }

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(uri("https://github.com/Puharesource/TitleManager/tree/master/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }

                perPackageOption {
                    matchingRegex.set("io.puharesource.mc.titlemanager.api.v2.*")
                    suppress.set(false)
                }

                perPackageOption {
                    matchingRegex.set("io.puharesource.mc.titlemanager.*")
                    suppress.set(true)
                }
            }
        }
    }

    val generateDocs by creating(Task::class) {
        dependsOn(dokkaHtml, dokkaJavadoc)
        group = "documentation"
    }

    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")

        dependsOn.add(dokkaJavadoc)
        from(dokkaJavadoc.get().outputDirectory)
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

    test {
        useJUnitPlatform()
    }

//    check {
//        dependsOn("installKotlinterPrePushHook")
//    }
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
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from("src/main/resources") {
        include("**/*.yml")
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("VERSION" to project.version))
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
    maven {
        name = "Vault"
        url = uri("https://nexus.hc.to/content/repositories/pub_releases")
    }

    maven {
        name = "ess-repo"
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "mvdw-software"
        url = uri("https://repo.mvdw-software.com/content/groups/public/")
    }

    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.google.dagger:dagger:2.40.5")
    kapt("com.google.dagger:dagger-compiler:2.40.5")

    implementation(project(":TitleManagerLib"))
    implementation(project(":TitleManagerBukkit:common"))
    implementation(group = "javax.inject", name = "javax.inject", version = "1")
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.6.0-native-mt")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "2.2.1")

    implementation(group = "com.destroystokyo.paper", name = "paper-api", version = "1.16.5-R0.1-SNAPSHOT")

    implementation(group = "be.maximvdw", name = "MVdWPlaceholderAPI", version = "3.0.1-SNAPSHOT") { isTransitive = false }
    implementation(group = "me.clip", name = "placeholderapi", version = "2.10.4")
    implementation(group = "net.milkbowl.vault", name = "VaultAPI", version = "1.7") { isTransitive = false }
    implementation(group = "net.ess3", name = "EssentialsX", version = "2.17.1") { isTransitive = false }
    implementation(group = "com.github.LeonMangler", name = "SuperVanish", version = "6.2.6-2") { isTransitive = false }
    implementation(group = "com.SirBlobman.combatlogx", name = "CombatLogX-API", version = "10.0.0.0-SNAPSHOT") { isTransitive = false }
    implementation(group = "com.SirBlobman.combatlogx.expansions", name = "Notifier", version = "10.0.0.0-SNAPSHOT") { isTransitive = false }

    fun dependOnVersions(vararg versions: String) {
        for (version in versions) {
            implementation(project(":TitleManagerBukkit:${version}")) { isTransitive = false }
        }
    }

    dependOnVersions(
        "v1_8_R1",
        "v1_8_R2",
        "v1_8_R3",
        "v1_9_R1",
        "v1_9_R2",
        "v1_10_R1",
        "v1_11_R1",
        "v1_12_R1",
        "v1_13_R1",
        "v1_13_R2",
        "v1_14_R1",
        "v1_15_R1",
        "v1_16_R1",
        "v1_16_R2",
        "v1_16_R3",
        "v1_17_R1",
        "v1_18_R1"
    )

    testImplementation(kotlin("test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
