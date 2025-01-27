package ru.hogeltbellai.CloudixNetwork.api.config;

import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;
import ru.hogeltbellai.CloudixNetwork.Debug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Configuration {
    private final Plugin plugin;
    private final String fileName;
    private net.md_5.bungee.config.Configuration config;

    public Configuration(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName + ".yml";

        createConfig();
        loadConfig();
    }

    private void createConfig() {
        File configFile = getConfigFile();

        if (!configFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }

                try (InputStream resource = plugin.getResourceAsStream(fileName)) {
                    if (resource != null) {
                        Files.copy(resource, configFile.toPath());
                        Debug.CORE.info("Файл конфигурации " + fileName + " создан.");
                    } else {
                        Debug.CORE.warning("Ресурс " + fileName + " не найден в jar-файле!");
                    }
                }
            } catch (IOException e) {
                Debug.CORE.severe("Не удалось создать файл конфигурации " + fileName + ": " + e.getMessage());
            }
        }
    }

    public void loadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getConfigFile());
            Debug.CORE.info("Конфигурация " + fileName + " успешно загружена.");
        } catch (IOException e) {
            Debug.CORE.severe("Ошибка загрузки конфигурации " + fileName + ": " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, getConfigFile());
            Debug.CORE.info("Конфигурация " + fileName + " успешно сохранена.");
        } catch (IOException e) {
            Debug.CORE.severe("Ошибка сохранения конфигурации " + fileName + ": " + e.getMessage());
        }
    }

    public net.md_5.bungee.config.Configuration getConfig() {
        return config;
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public Object get(String path, Object def) {
        return config.get(path, def);
    }

    private File getConfigFile() {
        return new File(plugin.getDataFolder(), fileName);
    }
}