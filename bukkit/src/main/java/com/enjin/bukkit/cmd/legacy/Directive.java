package com.enjin.bukkit.cmd.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Directive {
    String parent();

    String value();

    String[] aliases() default {};

    boolean requireValidKey() default true;
}
