package com.skitbet.jackiro.command.annotation;


import com.skitbet.jackiro.module.Module;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String name();
    String description();
    Module module() default Module.NONE;
    Permission[] permissions() default {};

    int cooldown() default 0;
}
