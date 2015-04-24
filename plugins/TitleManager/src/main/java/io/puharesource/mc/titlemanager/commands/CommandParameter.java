package io.puharesource.mc.titlemanager.commands;

public final class CommandParameter {

    private final String param;
    private final String value;

    public CommandParameter(final String param, final String value) {
        this.param = param;
        this.value = value;
    }

    public String getParameter() {
        return param;
    }

    public String getValue() {
        return value;
    }
}
