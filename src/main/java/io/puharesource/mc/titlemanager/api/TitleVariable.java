package ca.webmc.MiniGames_Core.framework;

public enum TitleVariable {
	PLAYERNAME("{PLAYER}"), DISPLAYNAME("{DISPLAYNAME}"), STRIPPEDDISPLAYNAME(
			"{STRIPPEDDISPLAYNAME}"), WORLD("{WORLD}"), WORLD_TIME(
			"{WORLD TIME}"), GROUP_NAME("{GROUP}"), ONLINE_PLAYERS("{ONLINE}"), MAX_PLAYERS(
			"{MAX PLAYERS}"), MONEY("{BALANCE}");// RAINBOW("{RAINBOW}"),
	// ONLINE_BUNGEE("{ONLINE: servername|ALL}"),MAX_BUNGEE("{MAX: servername|ALL}");

	private String text;

	TitleVariable(String var) {
		text = var;
	}

	public String getText() {
		return text;
	}

	public static TitleVariable getFromString(String var)
    {
        for(TitleVariable tv : TitleVariable.values())
        {
        	if(tv.getText().equalsIgnoreCase(var))
        	{
        		return tv;
        	}
        }
        return null;
    }
}
