package io.puharesource.mc.titlemanager.api.variables;

public @interface Variable {
    String[] vars();
    String hook() default "";
    String rule() default "";
}
