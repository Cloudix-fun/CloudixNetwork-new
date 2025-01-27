package ru.hogeltbellai.CloudixNetwork.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class S {

    public static void runSync(Runnable task, JavaPlugin plugin) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runAsync(Runnable task, JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runSyncDelayed(Runnable task, long delay, JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    public static void runAsyncDelayed(Runnable task, long delay, JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    public static void delay(long seconds, JavaPlugin plugin, Runnable task) {
        long delayTicks = seconds * 20;
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
}
