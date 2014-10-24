TitleManager
============
__TitleManager__ adds floating messages and a header/footer for your tabmenu. _TitleManager_ also provides you with a rich API, that you can use in your plugins.

#### Commands
- /tm reload - Reloads the config.
- /tm broadcast <message> - Sends a floating message to all players on the server. (Devide messages up into 2 with lines using <nl> inside of your message)
- /tm msg <player> <message> - Sends a floating message to a specific player on the server. (Devide messages up into 2 with lines using <nl> inside of your message)

#### Permissions
- reload - `titlemanager.commands.reload`
- broadcast - `titlemanager.commands.broadcast`
- msg - `titlemanager.commands.message`

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

##### Creating simple functions using the objects.
###### Simple title functions
This will simply show a given title and subtitle on the players screen.
````java
void sendFloatingText(Player player, String title, String subtitle) {
  new TitleObject(title, subtitle).send(player);
}
````
This will show a given title (without a subtitle) on the players screen, with a given fade in, stay and fade out time.
````java
void sendFloatingText(Player player, String title, int fadeIn, int stay, int fadeOut) {
  new TitleObject(title, TitleObject.Position.TITLE).setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut).send(player);
}
````
##### Simple header/footer functions
This will change the header and footer of a given player's tabmenu.
````java
void sendHeaderAndFooter(Player player, String header, String footer) {
  new TabTitleObject(header, footer).send(player);
}
````
This will change just the header of a given player's tabmenu.
````java
void sendHeader(Player player, String header) {
  new TabTitleObject(header, TabTitleObject.Position.HEADER).send(player);
}
````
##### Simple actionbar functions
````java
void sendActionbarMessage(Player player, String message) {
  new ActionbarTitleObject(message).send(player);
}
````
### The Events.
__TitleManager__ provides you with three custom _Cancellable_ events. One being `TitleEvent.java` which allows you to control or cancel the outcome of a Title being sent to a player, the second being `TabTitleChangeEvent.java` which allows you to control or cancel the outcome of a Tabmenu header or footer update being sent to a player and then last but not least, the `ActionbarEvent.java` which allows you to control or cancel the outcome of a Actionbar messages being sent.

Simply use these like any other Event, e.g:
````java
@EventHandler
public void onTitleSend(TitleEvent event) {
  if (true)
    event.setCancelled(true);
  else event.setCancelled(false);
}

@EventHandler
public void onTabTitleChange(TabTitleChangeEvent event) {
  if (true)
    event.setCancelled(true);
  else event.setCancelled(false);
}

@EventHandler
public void onActionbarMessage(ActionbarEvent event) {
  if (true)
    event.setCancelled(true);
  else event.setCancelled(false);
}
````
