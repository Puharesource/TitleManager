plugins {
    java
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("libs/spigot.jar"))
    implementation(project(":TitleManagerBukkit:common"))
}
