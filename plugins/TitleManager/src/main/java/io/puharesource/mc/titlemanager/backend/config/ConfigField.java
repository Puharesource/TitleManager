package io.puharesource.mc.titlemanager.backend.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField {
    String path();
}
