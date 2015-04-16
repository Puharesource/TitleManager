package io.puharesource.mc.titlemanager.backend.variables;

/**
 * Created by Tarkan on 16-04-2015.
 * This class is under the GPLv3 license.
 */
public @interface Variable {
    String[] vars();
    String hook() default "";
}
