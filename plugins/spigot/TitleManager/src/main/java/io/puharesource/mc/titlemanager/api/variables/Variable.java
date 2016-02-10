package io.puharesource.mc.titlemanager.api.variables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Variable {
    String[] vars();
    String hook() default "";
    String rule() default "";
}
