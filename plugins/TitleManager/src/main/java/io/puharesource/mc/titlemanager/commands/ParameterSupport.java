package io.puharesource.mc.titlemanager.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterSupport {
    String[] supportedParams();
}
