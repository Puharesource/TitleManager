plugins {
    base
    kotlin("multiplatform") version "2.3.21" apply false
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.serialization") version "2.3.21" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.8" apply false
    id("org.jetbrains.dokka") version "2.2.0" apply false
    id("org.jetbrains.dokka-javadoc") version "2.2.0" apply false
    id("org.jetbrains.qodana") version "2026.1.0" apply false
    id("org.sonarqube") version "7.3.0.8198"
}

group = "dev.tarkan.titlemanager"
version = providers.gradleProperty("titleManagerCoreVersion").get()

sonar {
    properties {
        property("sonar.projectKey", "tarkan_titlemanager")
        property("sonar.organization", "tarkan")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

repositories {
    mavenCentral()
}

subprojects {
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
    }
}

val npmCommand = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "npm.cmd" else "npm"
val titleManagerSupportedMinecraftVersions = providers.gradleProperty("titleManagerSupportedMinecraftVersions")
val titleManagerPaperApiVersion = providers.gradleProperty("titleManagerPaperApiVersion")

val validateSupportedMinecraftVersions = tasks.register("validateSupportedMinecraftVersions") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Validate marketplace Minecraft version metadata covers the configured Paper API version."
    val supportedVersions = titleManagerSupportedMinecraftVersions
    val paperApiVersion = titleManagerPaperApiVersion
    inputs.property("titleManagerSupportedMinecraftVersions", supportedVersions)
    inputs.property("titleManagerPaperApiVersion", paperApiVersion)

    doLast {
        val latestSupportedVersion = supportedVersions.get()
            .substringAfterLast('-')
            .substringBefore('x')
            .trimEnd('.')
        val configuredPaperVersion = paperApiVersion.get().substringBefore("-R")

        check(configuredPaperVersion.startsWith(latestSupportedVersion)) {
            "titleManagerSupportedMinecraftVersions=${supportedVersions.get()} does not cover titleManagerPaperApiVersion=${paperApiVersion.get()}"
        }
    }
}

val publicMavenRepositoryDirectory = layout.buildDirectory.dir("maven-repository")

val syncPublicMavenRepository = tasks.register<Sync>("syncPublicMavenRepository") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Collect public Maven publications for repo.tarkan.dev."
    dependsOn(
        ":modules:core:publishAllPublicationsToTitleManagerLocalRepository",
        ":modules:bukkit:api:publishAllPublicationsToTitleManagerLocalRepository"
    )

    into(publicMavenRepositoryDirectory)
    from(project(":modules:core").layout.buildDirectory.dir("titlemanager-local-maven"))
    from(project(":modules:bukkit:api").layout.buildDirectory.dir("titlemanager-local-maven"))
}

tasks.register<Zip>("bundlePublicMavenRepository") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Bundle public Maven publications for GitHub Release assets."
    dependsOn(syncPublicMavenRepository)
    archiveFileName.set("titlemanager-maven-repository-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(publicMavenRepositoryDirectory)
}

tasks.register<Zip>("bundleApiReferenceDocs") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Bundle generated JavaDoc and Dokka API reference docs for GitHub Release assets."
    dependsOn("syncApiReferenceDocs")
    archiveFileName.set("titlemanager-api-reference-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(layout.projectDirectory.dir("docs/site/static/api"))
}

tasks.register<Zip>("bundleDocsSite") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Bundle the generated Docusaurus docs site for GitHub Release assets."
    dependsOn("buildDocsSite")
    archiveFileName.set("titlemanager-docs-site-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(layout.projectDirectory.dir("docs/site/build"))
}

tasks.register<Copy>("preparePluginReleaseJar") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Copy the shaded plugin jar to the release asset name."
    dependsOn(":apps:bukkit-plugin:shadowJar")
    val pluginVersion = providers.gradleProperty("titleManagerJvmVersion")
    from(project(":apps:bukkit-plugin").layout.buildDirectory.file("libs/bukkit-plugin-${pluginVersion.get()}-all.jar"))
    into(layout.buildDirectory.dir("release-assets"))
    rename { "TitleManager-${pluginVersion.get()}.jar" }
}

tasks.register("prepareReleaseAssets") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Prepare release assets for GitHub, Hangar, and optional Spigot publishing."
    dependsOn("preparePluginReleaseJar", "bundlePublicMavenRepository", "bundleApiReferenceDocs", "bundleDocsSite")
}

val installWebViewer = tasks.register<Exec>("installWebViewer") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Install the TitleManager web viewer npm dependencies."
    workingDir = layout.projectDirectory.dir("apps/web-viewer").asFile
    commandLine(npmCommand, "ci", "--quiet")

    inputs.files(
        layout.projectDirectory.file("apps/web-viewer/package.json"),
        layout.projectDirectory.file("apps/web-viewer/package-lock.json")
    )
    outputs.dir(layout.projectDirectory.dir("apps/web-viewer/node_modules"))
}

val copyWebViewerCore = tasks.register<Exec>("copyWebViewerCore") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Copy the generated Kotlin/JS core package into the web viewer workspace."
    dependsOn(":modules:core:jsNodeProductionLibraryDistribution")
    workingDir = layout.projectDirectory.dir("apps/web-viewer").asFile
    commandLine("node", "scripts/copy-core.mjs")

    inputs.dir(layout.projectDirectory.dir("modules/core/build/dist/js/productionLibrary"))
    outputs.dir(layout.projectDirectory.dir("apps/web-viewer/.generated/titlemanager-core"))
}

val buildWebViewer = tasks.register<Exec>("buildWebViewer") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Build the TitleManager web viewer against the generated shared core package."
    dependsOn(installWebViewer, copyWebViewerCore)
    workingDir = layout.projectDirectory.dir("apps/web-viewer").asFile
    commandLine(npmCommand, "run", "build:no-prepare", "--", "--emptyOutDir")

    inputs.files(fileTree("apps/web-viewer/src"), "apps/web-viewer/index.html", "apps/web-viewer/tsconfig.json", "apps/web-viewer/vite.config.ts")
    inputs.dir(layout.projectDirectory.dir("apps/web-viewer/.generated/titlemanager-core"))
    outputs.dir(layout.projectDirectory.dir("apps/web-viewer/dist"))
}

val installDocsSite = tasks.register<Exec>("installDocsSite") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Install the TitleManager Docusaurus documentation site npm dependencies."
    workingDir = layout.projectDirectory.dir("docs/site").asFile
    commandLine(npmCommand, "ci", "--quiet")

    inputs.files(
        layout.projectDirectory.file("docs/site/package.json"),
        layout.projectDirectory.file("docs/site/package-lock.json")
    )
    outputs.dir(layout.projectDirectory.dir("docs/site/node_modules"))
}

val copyDocsSiteCore = tasks.register<Exec>("copyDocsSiteCore") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Copy the generated Kotlin/JS core package into the Docusaurus docs site workspace."
    dependsOn(":modules:core:jsNodeProductionLibraryDistribution")
    workingDir = layout.projectDirectory.dir("docs/site").asFile
    commandLine("node", "scripts/copy-core.mjs")

    inputs.dir(layout.projectDirectory.dir("modules/core/build/dist/js/productionLibrary"))
    outputs.dir(layout.projectDirectory.dir("docs/site/.generated/titlemanager-core"))
}

val syncApiReferenceDocs = tasks.register<Sync>("syncApiReferenceDocs") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Generate and copy JavaDoc and Dokka Kotlin API reference docs into the Docusaurus static directory."
    dependsOn(
        ":modules:core:dokkaGeneratePublicationHtml",
        ":modules:bukkit:api:dokkaGeneratePublicationHtml",
        ":modules:bukkit:api:dokkaGeneratePublicationJavadoc"
    )

    into(layout.projectDirectory.dir("docs/site/static/api"))
    into("kotlin") {
        from(layout.projectDirectory.dir("modules/bukkit/api/build/dokka/html"))
    }
    into("kotlin/titlemanager-core") {
        from(layout.projectDirectory.dir("modules/core/build/dokka/html"))
    }
    into("javadoc") {
        from(layout.projectDirectory.dir("modules/bukkit/api/build/dokka/javadoc"))
    }
}

val buildDocsSite = tasks.register<Exec>("buildDocsSite") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Build the TitleManager Docusaurus docs site and Vite/Rolldown/Oxc preview entry."
    dependsOn(installDocsSite, copyDocsSiteCore, syncApiReferenceDocs)
    workingDir = layout.projectDirectory.dir("docs/site").asFile
    commandLine(npmCommand, "run", "check")

    inputs.files(fileTree("docs/site/docs"), fileTree("docs/site/src"), "docs/site/index.html", "docs/site/sidebars.ts", "docs/site/docusaurus.config.ts", "docs/site/tsconfig.json", "docs/site/vite.config.ts")
    inputs.dir(layout.projectDirectory.dir("docs/site/.generated/titlemanager-core"))
    inputs.dir(layout.projectDirectory.dir("docs/site/static/api"))
    outputs.dir(layout.projectDirectory.dir("docs/site/build"))
    outputs.dir(layout.projectDirectory.dir("docs/site/vite-dist"))
}


val auditWebViewer = tasks.register<Exec>("auditWebViewer") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Audit production dependencies for the TitleManager web viewer."
    dependsOn(installWebViewer)
    workingDir = layout.projectDirectory.dir("apps/web-viewer").asFile
    commandLine(npmCommand, "audit", "--omit=dev", "--audit-level=moderate")
}

val checkWebViewer = tasks.register("checkWebViewer") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Build and audit the TitleManager web viewer."
    dependsOn(buildWebViewer, auditWebViewer)
}

tasks.named("check") {
    dependsOn(validateSupportedMinecraftVersions)
    dependsOn(checkWebViewer, buildDocsSite)
}
