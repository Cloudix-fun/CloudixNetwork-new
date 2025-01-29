package ru.hogeltbellai.CloudixNetwork.api.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.utils.U;

import java.lang.reflect.Field;
import java.util.*;

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

        public Builder enchant(String enchantments) {
            Arrays.stream(enchantments.split(","))
                    .map(entry -> entry.split(":"))
                    .filter(parts -> parts.length == 2)
                    .map(parts -> new AbstractMap.SimpleEntry<>(Enchantment.getByName(parts[0].toUpperCase()), Integer.parseInt(parts[1])))
                    .filter(entry -> entry.getKey() != null)
                    .forEach(entry -> itemsAPI.item.addUnsafeEnchantment(entry.getKey(), entry.getValue()));
            return this;
        }

        public Builder displayName(String displayName) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(U.colored(displayName));
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder lore(String... lore) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setLore(Arrays.asList(U.colored(lore)));
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

        public Builder headPlayerValue(String value) {
            SkullMeta meta = (SkullMeta) itemsAPI.item.getItemMeta();
            assert meta != null;

            setSkullTexture(meta, value);

            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder headPlayerName(String playerName) {
            SkullMeta meta = (SkullMeta) itemsAPI.item.getItemMeta();
            assert meta != null;

            if (Bukkit.getPlayer(playerName) != null) {
                String texture = getPlayerSkinBase64(playerName);
                setSkullTexture(meta, texture);
            } else {
                setSkullTexture(meta, getBase64TextureOffline());
            }

            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder headPlayerOffline(String offlinePlayerName) {
            SkullMeta meta = (SkullMeta) itemsAPI.item.getItemMeta();
            assert meta != null;

            String texture = getOfflineSkinBase64(offlinePlayerName);
            setSkullTexture(meta, texture);

            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder disableInteraction() {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            NamespacedKey key = new NamespacedKey(CNPluginSpigot.getPlugin(CNPluginSpigot.class), "no_interact");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, "true");
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public ItemsAPI build() {
            return itemsAPI;
        }

        private void setSkullTexture(SkullMeta meta, String texture) {
            GameProfile profile = new GameProfile(Bukkit.getPlayer(texture) != null ? Bukkit.getPlayer(texture).getUniqueId() : new UUID(0, 0), texture);
            profile.getProperties().put("textures", new Property("textures", texture));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getPlayerSkinBase64(String playerName) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjIyNGUxMTVlY2NhY2U4YmM1M2Q5N2RjZjg2M2QxNDRhNWMzOWZkMTEyM2FmZWMyNmE5ZDhmN2ZlZmQ2YWE1OSJ9fX0=";
        }

        private String getOfflineSkinBase64(String playerName) {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjIyNGUxMTVlY2NhY2U4YmM1M2Q5N2RjZjg2M2QxNDRhNWMzOWZkMTEyM2FmZWMyNmE5ZDhmN2ZlZmQ2YWE1OSJ9fX0=";
        }

        private String getBase64TextureOffline() {
            return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjIyNGUxMTVlY2NhY2U4YmM1M2Q5N2RjZjg2M2QxNDRhNWMzOWZkMTEyM2FmZWMyNmE5ZDhmN2ZlZmQ2YWE1OSJ9fX0=";
        }
    }

    public static class InteractionItemListener implements Listener {

        @org.bukkit.event.EventHandler
        public void onClick(InventoryClickEvent event) {
            if (event.getCurrentItem() != null && hasNoInteract(event.getCurrentItem())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onInteract(PlayerInteractEvent event) {
            if (event.getItem() != null && hasNoInteract(event.getItem())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onDropItem(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack() != null && hasNoInteract(event.getItemDrop().getItemStack())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onMoveItem(InventoryMoveItemEvent event) {
            if (event.getItem() != null && hasNoInteract(event.getItem())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onPlace(BlockPlaceEvent event) {
            if (event.getItemInHand() != null && hasNoInteract(event.getItemInHand())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onConsume(PlayerItemConsumeEvent event) {
            if (event.getItem() != null && hasNoInteract(event.getItem())) {
                event.setCancelled(true);
            }
        }

        @org.bukkit.event.EventHandler
        public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
            if (event.getOffHandItem() != null && hasNoInteract(event.getOffHandItem())) {
                event.setCancelled(true);
            }
            if (event.getMainHandItem() != null && hasNoInteract(event.getMainHandItem())) {
                event.setCancelled(true);
            }
        }

        private boolean hasNoInteract(ItemStack item) {
            if (item == null || item.getType() == Material.AIR) return false;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            NamespacedKey key = new NamespacedKey(CNPluginSpigot.getPlugin(CNPluginSpigot.class), "no_interact");
            return meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
    }
}