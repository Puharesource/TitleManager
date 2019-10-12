import org.apache.tools.ant.filters.ReplaceTokens
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`

    kotlin("jvm") version "1.3.50"

    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.9.18"
    id("net.saliman.properties") version "1.5.1"
}

group = "io.puharesource.mc"
version = "2.1.6"

tasks.withType<Assemble> {
    dependsOn.add(tasks.getting(ShadowJar::class))
}

tasks.withType<Jar> {
    enabled = false
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<ShadowJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveBaseName.set("TitleManager")
    archiveClassifier.set(null as String?)

    dependencies {
        include(dependency("org.jetbrains.kotlin:.*"))
        include(dependency("io.reactivex:.*"))
    }

    relocate("rx", "io.puharesource.mc.titlemanager.shaded.rx")
    relocate("kotlin", "io.puharesource.mc.titlemanager.shaded.kotlin")
    relocate("org.jetbrains", "io.puharesource.mc.titlemanager.shaded.org.jetbrains")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")

        dependsOn.add(this@tasks.getting(DokkaTask::class))
        from(this@tasks["dokka"])
    }

    val apiJar by creating(Jar::class) {
        archiveClassifier.set("api")

        include("io/puharesource/mc/titlemanager/api/v2/**")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Puharesource/grp_test")

            credentials {
                username = (findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")) as? String
                password = (findProperty("gpr.key") ?: System.getenv("GPR_API_KEY")) as? String
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            // from(components["java"])
        }
    }
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/docs/javadoc"

    jdkVersion = 8

    impliedPlatforms = mutableListOf("JVM")

    linkMapping {
        dir = "src/main/kotlin"
        url = "https://github.com/Puharesource/TitleManager/tree/master/src/main/kotlin"
        suffix = "#L"
    }

    externalDocumentationLink {
        url = uri("https://hub.spigotmc.org/javadocs/spigot/").toURL()
    }

    packageOptions {
        prefix = "io.puharesource.mc.titlemanager"
        suppress = true
    }

    packageOptions {
        prefix = "io.puharesource.mc.titlemanager.api.v2"
        suppress = false
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
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.50")
    implementation(group = "io.reactivex", name = "rxjava", version = "1.3.8")

    implementation(group = "org.spigotmc", name = "spigot-api", version = "1.11-R0.1-SNAPSHOT")

    implementation(group = "be.maximvdw", name = "MVdWPlaceholderAPI", version = "3.0.1-SNAPSHOT") { isTransitive = false }
    implementation(group = "me.clip", name = "placeholderapi", version = "2.10.4")
    implementation(group = "net.milkbowl.vault", name = "VaultAPI", version = "1.7")
    implementation(group = "net.ess3", name = "EssentialsX", version = "2.17.1") { isTransitive = false }

    implementation(fileTree("$rootDir/libs"))

    implementation(group = "org.graalvm", name = "graal-sdk", version = "1.0.0-rc7")
    implementation(group = "com.oracle.truffle", name = "truffle-api", version = "1.0.0-rc7")

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit", version = "1.3.50")
}
