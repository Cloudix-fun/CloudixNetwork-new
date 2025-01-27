package ru.hogeltbellai.CloudixNetwork.api.pet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

@Getter
public class PetAPI {

    private String name;
    private ItemStack headItem;

    public PetAPI(String name, ItemStack headItem) {
        this.name = name;
        this.headItem = headItem;
    }

    public PetAPI(ConfigurationSection config) {
        this.name = config.getString("name");
        this.headItem = loadHeadItem(config);
    }

    private ItemStack loadHeadItem(ConfigurationSection config) {
        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) headItem.getItemMeta();

        String base64Texture = config.getString("head_texture");
        if (base64Texture != null && !base64Texture.isEmpty()) {
            try {
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", base64Texture));

                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        headItem.setItemMeta(meta);
        return headItem;
    }
}
