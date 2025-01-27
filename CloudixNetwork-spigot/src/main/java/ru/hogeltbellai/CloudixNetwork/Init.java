package ru.hogeltbellai.CloudixNetwork;

import ru.hogeltbellai.CloudixNetwork.api.config.Configuration;
import ru.hogeltbellai.CloudixNetwork.mysql.Database;

public class Init {
    private CNPluginSpigot plugin;

    public Init(CNPluginSpigot plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        plugin.database = new Database(
                plugin.getConfig().getString("mysql.jdbcUrl"),
                plugin.getConfig().getString("mysql.username"),
                plugin.getConfig().getString("mysql.password"),
                plugin.getConfig().getInt("mysql.poolMax")
        );
    }

    public void initConfig() {
        plugin.conf = new Configuration(plugin, "config");
        plugin.conf = new Configuration(plugin, "dynamic");
    }
}