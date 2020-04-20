<h1 align="center">
    TitleManager
</h1>

<p align="center">
    <a href="https://www.spigotmc.org/resources/titlemanager.1049">
        <img src="https://img.shields.io/badge/Version-2.1.7-green"
             alt="Version">
    </a>
    <a href="https://github.com/Puharesource/TitleManager/actions">
        <img src="https://github.com/Puharesource/TitleManager/workflows/Java%20CI/badge.svg"
             alt="Actions Status">
    </a>
    <a href="https://tarkan.dev/javadoc/titlemanager/">
        <img src="https://img.shields.io/badge/JavaDoc-2.1-blue.svg"
             alt="Javadoc">
    </a>
    <a href="https://www.spigotmc.org/resources/titlemanager.1049/updates">
        <img src="https://img.shields.io/badge/Minecraft-1.7%20--%201.15.2-blue"
             alt="Minecraft versions">
    </a>
</p>

<p align="center"><sup><strong>A Bukkit plugin for sending titles and setting the header and footer of the player list. <a href="https://www.spigotmc.org/resources/titlemanager.1049/">Spigot Project Page</a></strong></sup></p>

---

* **[WIKI](https://github.com/Puharesource/TitleManager/wiki)** – plugin guidelines.
* **[COMMANDS](https://github.com/Puharesource/TitleManager/wiki/commands)** – command guidelines.
* **[PERMISSIONS](https://github.com/Puharesource/TitleManager/wiki/permissions)** – command and feature permissions.
* **[SUPPORT CHAT](https://discord.gg/U3Yyu6G)** - discord support chat and help.

Sponsors
--------

<a href="https://mineacademy.org/invite?rl=epqj"><img src="https://i.imgur.com/uIAi9Lx.png"></a>

For Developers
--------------

#### The Repository
Example for Gradle:
```groovy
maven {
    name 'puharesource-repo'
    url 'http://repo.puha.io/repo/'
}
```

Example for Maven:
```xml
<repository>
  <id>puha-repo</id>
  <url>http://repo.puha.io/repo/</url>
</repository>
```

#### The dependency
Example for Gradle:
```groovy
compile group: 'io.puharesource.mc', name: 'TitleManager', version: '2.0.0'
```  

Example for Maven
```xml
<dependency>
   <groupId>io.puharesource.mc</groupId>
   <artifactId>TitleManager</artifactId>
   <version>2.0.0</version>
</dependency>
```

### plugin.yml
If your plugin can't run without TitleManager add the following line to your plugin.yml file.  
```yaml
depend: [TitleManager]
```

If your plugin can run without TitleManager, then add the following line to your plugin.yml file instead
```yaml
softdepend: [TitleManager]
```

### Getting the API instance
Once you want to use TitleManager's API, you'll need an instance of `TitleManagerAPI`, which carries all of the methods available for TitleManager. I suggest getting the instance once you load your plugin and store it somewhere easily accessible, for this example I'll however just be storing it locally in the `onEnable` method.

##### Java
```java
@Override
public void onEnable() {
  TitleManagerAPI api = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManager");
}
```

##### Kotlin
For kotlin I suggest using the `lazy` delegate for storing the instance of TitleManager when accessed.  
```kotlin
val titleManagerAPI : TitleManagerAPI by lazy { Bukkit.getServer().pluginManager.getPlugin("TitleManager") }
```
