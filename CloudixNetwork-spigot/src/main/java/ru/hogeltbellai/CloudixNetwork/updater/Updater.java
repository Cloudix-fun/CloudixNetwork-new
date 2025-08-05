package ru.hogeltbellai.CloudixNetwork.updater;

import org.bukkit.Bukkit;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.Debug;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {

    public static String performUpdate(String fileName) throws Exception {
        String updateUrl = CNPluginSpigot.core().getConfig().getString("updater.url") + "/" + fileName;

        URL url = new URL(updateUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        File pluginsFolder = new File(Bukkit.getServer().getWorldContainer(), "plugins");
        File oldPluginFile = new File(pluginsFolder, fileName);

        if (oldPluginFile.exists()) {
            if (oldPluginFile.delete()) {
                Debug.CORE.info("Старый плагин удален: " + oldPluginFile.getName());
            }
        }

        File newPluginFile = new File(pluginsFolder, fileName);

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(newPluginFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        if (!newPluginFile.exists()) {
            return "Ошибка: не удалось скачать новый плагин";
        }

        Debug.CORE.info("Обновление установлено");

        return "Обновление завершено успешно: " + newPluginFile.getName();
    }
}
