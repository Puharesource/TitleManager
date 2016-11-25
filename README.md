TitleManager
============
__TitleManager__ adds floating titles and a header/footer for your player list. _TitleManager_ also provides you with a rich API, that can be used in other plugins.
#### [Wiki](https://github.com/Puharesource/TitleManager/wiki)
* [Commands](https://github.com/Puharesource/TitleManager/wiki/commands)
* [Permissions](https://github.com/Puharesource/TitleManager/wiki/permissions)
* [Download / Forums](http://www.spigotmc.org/resources/titlemanager.1049/)

For Developers
--------------
**JavaDocs**
https://jd.puharesource.io/titlemanager/

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
