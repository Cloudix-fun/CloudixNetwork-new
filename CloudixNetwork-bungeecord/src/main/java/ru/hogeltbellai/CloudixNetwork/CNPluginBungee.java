package ru.hogeltbellai.CloudixNetwork;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import ru.hogeltbellai.CloudixNetwork.api.config.Configuration;
import ru.hogeltbellai.CloudixNetwork.commands.SeenCommand;
import ru.hogeltbellai.CloudixNetwork.core.BungeePacketHandler;
import ru.hogeltbellai.CloudixNetwork.impl.MysqlPlayer;
import ru.hogeltbellai.CloudixNetwork.mysql.Database;
import ru.hogeltbellai.Core.connector.CoreNetwork;

@Getter
public final class CNPluginBungee extends Plugin {
    private static CNPluginBungee instance;
    public CoreNetwork coreNetwork;
    public Database database;
    public Configuration conf;
    public MysqlPlayer mysqlPlayer;

    @Override
    public void onEnable() {
        instance = this;

        conf = new Configuration(this, "config");

        new Init(this).initDatabase();

        Debug.setGlobalEnabled(conf.getConfig().getBoolean("debug"));

        coreNetwork = new CoreNetwork(
                conf.getConfig().getBoolean("core.enabled"),
                conf.getConfig().getString("core.host"),
                conf.getConfig().getInt("core.port"));

        coreNetwork.setMainHandler(new BungeePacketHandler(this));
        coreNetwork.connect();

        mysqlPlayer = new MysqlPlayer(database);

        getProxy().getPluginManager().registerCommand(this, new SeenCommand());
    }

    @Override
    public void onDisable() {
        coreNetwork.disconnect();
    }

    public static CNPluginBungee core() {
        return instance;
    }
}
