package io.puharesource.mc.titlemanager.api;

/**
 * The variable class.
 * This class consists of all of the placeholders that TitleManager offers.
 */
public enum TitleVariable {
    PLAYER_NAME("PLAYER"),
    DISPLAY_NAME("DISPLAYNAME"),
    STRIPPED_DISPLAY_NAME("STRIPPEDDISPLAYNAME"),
    WORLD("WORLD"),
    WORLD_TIME("WORLD-TIME"),
    GROUP_NAME("GROUP"),
    ONLINE_PLAYERS("ONLINE"),
    MAX_PLAYERS("MAX-PLAYERS"),
    BALANCE("BALANCE");

    private String text;

    TitleVariable(String variable) {
        text = variable;
    }

    public static TitleVariable getFromString(String var) {
        var = var.replace(" ", "-");
        for (TitleVariable variable : TitleVariable.values())
            if (variable.getText().equalsIgnoreCase(var) || variable.getTextRaw().equalsIgnoreCase(var))
                return variable;
        return null;
    }

    public String getTextRaw() {
        return "{" + text + "}";
    }

    public String getText() {
        return text;
    }
}
