TitleManager
============
__TitleManager__ adds floating messages and a header/footer for your tabmenu. _TitleManager_ also provides you with a rich API, that you can use in your plugins.
#### [Wiki](https://github.com/Puharesource/TitleManager/wiki)
* [Commands](https://github.com/Puharesource/TitleManager/wiki/commands)
* [Permissions](https://github.com/Puharesource/TitleManager/wiki/permissions)
* [Download / Forums](http://www.spigotmc.org/resources/titlemanager.1049/)

The API
---------

## Using TitleManager
_To use TitleManager within your plugin, make sure to have done the following._
First add TitleManager-X.X.X.jar to your build path. Then, add TitleManager as a dependency to your plugin.yml file:
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
Now you should be good to go and ready to use the API inside of your plugins!

### The Objects
__TitleManager__ provides you with three objects, one being `TitleObject.java` which controls everything to do with the floating messages on your screen, and the second being `TabTitleObject.java` which controls everything to do with the header and footer on the Tabmenu (aka the playerlist) and last but not least `ActionbarTitleObject.java` which controls everything to do with Actionbar titles, like sending an actionbar title.

* [Title Object](https://github.com/Puharesource/TitleManager/wiki/api-title)
* [Tabmenu Object](https://github.com/Puharesource/TitleManager/wiki/api-tab)
* [Actionbar Title Object](https://github.com/Puharesource/TitleManager/wiki/api-actionbar)
