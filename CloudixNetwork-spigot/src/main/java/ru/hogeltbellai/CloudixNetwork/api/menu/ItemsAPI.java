package ru.hogeltbellai.CloudixNetwork.api.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

@Getter
public class ItemsAPI {
    private final ItemStack item;

    public ItemsAPI() {
        item = new ItemStack(Material.STONE);
    }

    public static class Builder {
        private final ItemsAPI itemsAPI;

        public Builder() {
            itemsAPI = new ItemsAPI();
        }

        public Builder material(Material material) {
            itemsAPI.item.setType(material);
            return this;
        }

        public Builder displayName(String displayName) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(displayName);
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder lore(String... lore) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setLore(Arrays.asList(lore));
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder hideFlags(ItemFlag... flags) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.addItemFlags(flags);
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder headPlayer(String playerName) {
            SkullMeta meta = (SkullMeta) itemsAPI.item.getItemMeta();
            assert meta != null;

            try {
                UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                String skinUrl = getPlayerSkinUrl(playerUUID);

                if (skinUrl != null) {
                    GameProfile profile = new GameProfile(playerUUID, playerName);
                    profile.getProperties().put("textures", new Property("textures", skinUrl));

                    Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta, profile);
                } else {
                    GameProfile profile = new GameProfile(playerUUID, playerName);
                    profile.getProperties().put("textures", new Property("textures", getDefaultSkin()));

                    Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta, profile);
                }

                itemsAPI.item.setItemMeta(meta);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        }

        public ItemsAPI build() {
            return itemsAPI;
        }

        private String getPlayerSkinUrl(UUID uuid) {
            try {
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String jsonResponse = response.toString();
                int start = jsonResponse.indexOf("\"value\":") + 8;
                int end = jsonResponse.indexOf("\"", start);
                String texture = jsonResponse.substring(start, end);

                return texture;
            } catch (Exception e) {
                return null;
            }
        }

        private String getDefaultSkin() {
            return "d41d8cd98f00b204e9800998ecf8427e";
        }
    }
}