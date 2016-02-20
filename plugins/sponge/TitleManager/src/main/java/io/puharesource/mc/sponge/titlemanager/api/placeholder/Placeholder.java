package io.puharesource.mc.sponge.titlemanager.api.placeholder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Placeholder {
    String[] placeholders();
    String hook() default "";
    String rule() default "";
}
