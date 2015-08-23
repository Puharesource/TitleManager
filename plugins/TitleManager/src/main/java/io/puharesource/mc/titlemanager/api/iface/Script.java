package io.puharesource.mc.titlemanager.api.iface;

public interface Script {

    String getName();
    String getVersion();
    String getAuthor();

    ScriptConverter getConverter();
}
