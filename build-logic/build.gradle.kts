plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("kotlinJvm") {
            id = "titlemanager.kotlin-jvm"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("kotlinMultiplatform") {
            id = "titlemanager.kotlin-multiplatform"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("bukkitPlugin") {
            id = "titlemanager.bukkit-plugin"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("nmsLegacy") {
            id = "titlemanager.nms-legacy"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("nmsPaperweight") {
            id = "titlemanager.nms-paperweight"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("docsSite") {
            id = "titlemanager.docs-site"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
        register("publication") {
            id = "titlemanager.publication"
            implementationClass = "dev.tarkan.titlemanager.buildlogic.NoopConventionPlugin"
        }
    }
}
