package io.puharesource.mc.titlemanager.api.variables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Variable {
    @Deprecated
    String[] vars();

    @Deprecated
    String hook() default "";

    @Deprecated
    String rule() default "";
}