package ru.hogeltbellai.CloudixNetwork.api.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.hogeltbellai.CloudixNetwork.Debug;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class Configuration {
    private static final Function<String, Location> LOCATION_PARSER;
    private final File configFile;
    private final FileConfiguration config;

    static {
        LOCATION_PARSER = (str -> {
            String[] parts = str.split(",");
            World world = Bukkit.getWorld(parts[0].trim());
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0;
            return new Location(world, x, y, z, yaw, pitch);
        });
    }

    public Configuration(Plugin plugin, String fileName) {
        this.configFile = new File(plugin.getDataFolder(), fileName + ".yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource(fileName + ".yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    plugin.getDataFolder().mkdirs();
                    Files.copy(plugin.getResource(fileName + ".yml"), configFile.toPath());
                } catch (IOException ioException) {
                    Debug.CORE.info("Не удалось создать файл: " + configFile.getName());
                }
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public Object get(String path, Object def) {
        return config.get(path, def);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public Location getLocation(String path) {
        String locationString = getString(path);
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        return LOCATION_PARSER.apply(locationString);
    }

    public List<Location> getLocationList(String path) {
        List<String> locationStrings = getStringList(path);
        List<Location> locations = new ArrayList<>();

        for (String locStr : locationStrings) {
            locations.add(LOCATION_PARSER.apply(locStr));
        }

        return locations;
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Debug.CORE.severe("Не удалось сохранить файл " + configFile.getName());
        }
    }

    public void reload() {
        try {
            config.load(configFile);
        } catch (Exception e) {
            Debug.CORE.severe("Не удалось запустить файл " + configFile.getName());
        }
    }
}