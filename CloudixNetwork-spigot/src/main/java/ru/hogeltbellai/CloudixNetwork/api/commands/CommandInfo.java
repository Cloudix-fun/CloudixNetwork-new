package ru.hogeltbellai.CloudixNetwork.api.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String name();                      // Основное имя команды
    String[] aliases() default {};       // Псевдонимы команды
    String permission() default "";      // Права для использования команды
    boolean forPlayer() default false;   // Команда доступна только для игроков?
    boolean forConsole() default false;  // Команда доступна только для консоли?
    boolean forAll() default false;      // Команда доступна для всех (игроков и консоли)?
    int[] playerTabComplete() default {}; // Индексы аргументов, для которых нужно автозавершение по игрокам
}

