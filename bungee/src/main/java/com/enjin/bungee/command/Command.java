package com.enjin.bungee.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Command {
    public String value();

    public String[] aliases() default {};

    public String description() default "";

    public boolean requireValidKey() default true;
}
