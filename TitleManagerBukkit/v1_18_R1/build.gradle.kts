plugins {
    java
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("libs/spigot.jar"))
    implementation(files("libs/spigot-api.jar"))
    implementation(files("libs/bungeecord-chat.jar"))
    implementation(files("libs/netty.jar"))
    implementation(project(":TitleManagerBukkit:common"))
}
