package ru.hogeltbellai.CloudixNetwork;

import ru.hogeltbellai.CloudixNetwork.mysql.Database;

public class Init {
    private CNPluginBungee plugin;

    public Init(CNPluginBungee plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        plugin.database = new Database(
                plugin.getConf().getConfig().getString("mysql.jdbcUrl"),
                plugin.getConf().getConfig().getString("mysql.username"),
                plugin.getConf().getConfig().getString("mysql.password"),
                plugin.getConf().getConfig().getInt("mysql.poolMax")
        );
    }
}
