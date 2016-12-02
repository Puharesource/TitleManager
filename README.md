<h1 align="center">
    TitleManager
</h1>

<p align="center">
    <a href="(https://travis-ci.org/Puharesource/TitleManager">
        <img src="https://travis-ci.org/Puharesource/TitleManager.svg?branch=master"
             alt="Build status">
    </a>
    <a href="https://jitpack.io/#Puharesource/TitleManager">
        <img src="https://jitpack.io/v/Puharesource/TitleManager.svg"
             alt="Maven repository">
    </a>
    <a href="https://jitpack.io/com/github/Puharesource/TitleManager/2.0.0-4/javadoc/">
        <img src="https://img.shields.io/badge/JitPack-javadoc--2.0.0-blue.svg"
             alt="Javadoc">
    </a>
</p>

<p align="center"><sup><strong>A Bukkit plugin for sending titles and setting the header and footer of the player list. <a href="https://www.spigotmc.org/resources/titlemanager.1049/">Spigot Project Page</a></strong></sup></p>

---

* **[WIKI](https://github.com/Puharesource/TitleManager/wiki)** – Plugin guidelines.
* **[COMMANDS](https://github.com/Puharesource/TitleManager/wiki/commands)** – command guidelines.
* **[PERMISSIONS](https://github.com/Puharesource/TitleManager/wiki/permissions)** – command and feature permissions.

For Developers
--------------

**Gradle repository**
````groovy
maven {
    name 'puharesource-repo'
    url 'http://repo.puha.io/nexus/content/repositories/releases/'
}
````

**Gradle dependency**
````groovy
compile group: 'io.puharesource.mc', name: 'TitleManager', version: '2.0.0'
````

**Maven repository**
````xml
<repository>
  <id>puha-repo</id>
  <url>http://repo.puha.io/nexus/content/repositories/releases/</url>
</repository>
````

**Maven dependency**
````xml
<dependency>
  <groupId>io.puharesource.mc</groupId>
  <artifactId>TitleManager</artifactId>
  <version>2.0.0</version>
</dependency>
````

---

**Adding TitleManager as a plugin dependency**
````yml
depend: [TitleManager]
````
Alternatively, you can add as a soft dependency `softdepend: [TitleManager]` if you can live without it.
Another alternative would be to check for the plugin within `onEnable()` inside of your Main class:
````java
@Override
public void onEnable() {
  if (getServer().getPluginManager().getPlugin("TitleManager") != null && getServer().getPluginManager().getPlugin("TitleManager").isEnabled())
    getLogger().info("Successfully hooked into TitleManager!");
  else {
    getLogger().warning("Failed to hook into TitleManager, disabling plugin!");
    getPluginLoader().disablePlugin(this);
  }
}
````
