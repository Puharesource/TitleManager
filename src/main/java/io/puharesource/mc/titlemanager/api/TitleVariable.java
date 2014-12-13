package io.puharesource.mc.titlemanager.api;

public enum TitleVariable {
    PLAYERNAME("{PLAYER}"),
    DISPLAYNAME("{DISPLAYNAME}"),
    STRIPPEDDISPLAYNAME("{STRIPPEDDISPLAYNAME}"),
    WORLD("{WORLD}"),
    WORLD_TIME("{WORLD TIME}"),
    GROUP_NAME("{GROUP}"),
    ONLINE_PLAYERS("{ONLINE}"),
    MAX_PLAYERS("{MAX PLAYERS}"),
    MONEY("{BALANCE}");
    /* RAINBOW("{RAINBOW}"),
    ONLINE_BUNGEE("{ONLINE: servername|ALL}"),
    MAX_BUNGEE("{MAX: servername|ALL}");*/

    private String text;

    TitleVariable(String variable) {
        text = variable;
    }

    public static TitleVariable getFromString(String var) {
        for (TitleVariable tv : TitleVariable.values())
            if (tv.getText().equalsIgnoreCase(var) || tv.getTextRaw().equalsIgnoreCase(var))
                return tv;
        return null;
    }

    public String getTextRaw() {
        return text;
    }

    public String getText() {
        String textTwo = text;
        if (text.startsWith("{") && text.endsWith("}")) textTwo = text.substring(1, text.length() - 1);
        return textTwo;
    }
}
