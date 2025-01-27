package ru.hogeltbellai.CloudixNetwork;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.CloudixNetwork.api.config.Configuration;
import ru.hogeltbellai.CloudixNetwork.api.menu.MenuAPI;
import ru.hogeltbellai.CloudixNetwork.commands.*;
import ru.hogeltbellai.CloudixNetwork.core.BukkitPacketHandler;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;
import ru.hogeltbellai.CloudixNetwork.listener.PlayerListener;
import ru.hogeltbellai.CloudixNetwork.mysql.Database;
import ru.hogeltbellai.Core.connector.CoreConnector;

@Getter
public final class CNPluginSpigot extends JavaPlugin {
    private static CNPluginSpigot instance;
    public Database database;
    public Configuration conf;
    private CoreConnector coreConnector;

    @Override
    public void onEnable() {
        instance = this;

        new Init(this).initConfig();
        new Init(this).initDatabase();

        Debug.setGlobalEnabled(getConfig().getBoolean("debug"));

        database.executeFile("schema.sql");

        coreConnector = new CoreConnector(getConfig().getBoolean("core.enabled"),
                getConfig().getString("core.host"),
                getConfig().getInt("core.port"));

        coreConnector.setMainHandler(new BukkitPacketHandler(this));
        coreConnector.connect();

        new CloudixCommand().register(this);
        new MsgCommand().register(this);
        new BanCommand().register(this);
        new MuteCommand().register(this);
        new IgnoreCommand().register(this);

        getServer().getPluginManager().registerEvents(new MenuAPI(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        CPlayerManager.startExpirationCheckTask();
    }

    @Override
    public void onDisable() {
        database.finish();

        if(coreConnector.isEnabled() && coreConnector.isConnected()) {
            coreConnector.connect();
        }
    }

    public static CNPluginSpigot core() {
        return instance;
    }
}