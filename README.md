<h1 align="center">
    TitleManagerReborn
</h1>

<p align="center">
    <a href="https://www.spigotmc.org/resources/titlemanagerreborn.123456/">
        <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge" alt="Version">
    </a>
    <a href="https://github.com/MinekartaStudio/TitleManagerReborn/actions">
        <img src="https://img.shields.io/github/workflow/status/MinekartaStudio/TitleManagerReborn/Java%20CI?logo=github&style=for-the-badge&logoColor=fff" alt="Actions Status">
    </a>
    <a href="https://www.spigotmc.org/resources/titlemanagerreborn.123456/">
        <img src="https://img.shields.io/badge/Minecraft-1.13%20--%201.18-blue?style=for-the-badge&logo=Hack-the-Box&logoColor=fff" alt="Minecraft versions">
    </a>
</p>

<p align="center"><sup><strong>A Bukkit plugin for sending titles and setting the header and footer of the player list. <a href="https://www.spigotmc.org/resources/titlemanagerreborn.123456/">Spigot Project Page</a></strong></sup></p>

---

* **[WIKI & GUIDES](https://github.com/MinekartaStudio/TitleManagerReborn/wiki)** – plugin guidelines.
* **[COMMANDS](https://github.com/MinekartaStudio/TitleManagerReborn/wiki/Commands)** – command guidelines.
* **[PERMISSIONS](https://github.com/MinekartaStudio/TitleManagerReborn/wiki/Permissions)** – command and feature permissions.
* **[SUPPORT CHAT](https://discord.gg/U3Yyu6G)** - discord support chat and help.

For Developers
--------------

#### The Repository
Example for Gradle .kts:
```kotlin
maven("https://maven.pkg.github.com/minekartastudio/titlemanagerreborn") {
    credentials {
        username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
        password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
}
```

Example for Gradle:
```groovy
maven {
    url 'https://maven.pkg.github.com/minekartastudio/titlemanagerreborn'
    credentials {
        username = project.findProperty('gpr.user') ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty('gpr.key') ?: System.getenv("GITHUB_TOKEN")
    }
}
```

Example for Maven:
```xml
<repository>
  <id>github</id>
  <url>https://maven.pkg.github.com/minekartastudio/titlemanagerreborn</url>
</repository>
```

#### The dependency
Example for Gradle .kts:
```kotlin
implementation('studio.minekarta:TitleManagerReborn:1.0.0')
```  

Example for Gradle:
```groovy
compile group: 'studio.minekarta', name: 'TitleManagerReborn', version: '1.0.0'
```

Example for Maven
```xml
<dependency>
   <groupId>studio.minekarta</groupId>
   <artifactId>TitleManagerReborn</artifactId>
   <version>1.0.0</version>
</dependency>
```

### plugin.yml
If your plugin can't run without TitleManagerReborn add the following line to your plugin.yml file.
```yaml
depend: [TitleManagerReborn]
```

If your plugin can run without TitleManagerReborn, then add the following line to your plugin.yml file instead
```yaml
softdepend: [TitleManagerReborn]
```

### Getting the API instance
Once you want to use TitleManagerReborn's API, you'll need an instance of `TitleManagerAPI`, which carries all of the methods available for TitleManager. I suggest getting the instance once you load your plugin and store it somewhere easily accessible, for this example I'll however just be storing it locally in the `onEnable` method.

##### Java
```java
@Override
public void onEnable() {
  TitleManagerAPI api = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManagerReborn");
}
```

##### Kotlin
For kotlin I suggest using the `lazy` delegate for storing the instance of TitleManager when accessed.  
```kotlin
val titleManagerAPI : TitleManagerAPI by lazy { Bukkit.getServer().pluginManager.getPlugin("TitleManagerReborn") as TitleManagerAPI }
```

---
This plugin is a reborn version of the original TitleManager by Puharesource/Tarkan.
The original plugin can be found [here](https://www.spigotmc.org/resources/titlemanager.1049/).
