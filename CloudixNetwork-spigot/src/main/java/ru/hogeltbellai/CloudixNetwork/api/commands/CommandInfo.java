package ru.hogeltbellai.CloudixNetwork.api.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String name();
    String permission() default "";
    boolean forPlayer() default false;
    int[] playerTabComplete() default {};
}
