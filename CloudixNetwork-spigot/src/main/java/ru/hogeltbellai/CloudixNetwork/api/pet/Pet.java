package ru.hogeltbellai.CloudixNetwork.api.pet;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Pet implements Listener {

    private final Map<UUID, CustomPet> playerPets = new ConcurrentHashMap<>();

    public void spawnPet(Player player, PetAPI petAPI, boolean isBaby) {
        Location petLocation = calculatePetLocation(player);
        CustomPet pet = new CustomPet(player, petLocation, petAPI, isBaby);
        playerPets.put(player.getUniqueId(), pet);
    }

    public void removePet(Player player) {
        CustomPet pet = playerPets.remove(player.getUniqueId());
        if (pet != null) {
            pet.removePet();
        }
    }

    public Location calculatePetLocation(Player player) {
        Location playerLocation = player.getEyeLocation();
        Vector playerDirection = playerLocation.getDirection().normalize();
        Vector rightDirection = playerDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Location petLocation = playerLocation.clone().add(rightDirection.multiply(2));
        petLocation.setY(petLocation.getY() - 1);
        return petLocation;
    }

    public PetAPI loadPetData(ConfigurationSection config, String petName) {
        String path = "pets." + petName;
        if (config.contains(path)) {
            return new PetAPI(config.getConfigurationSection(path));
        }
        return null;
    }

    public boolean hasPet(Player player) {
        return playerPets.containsKey(player.getUniqueId());
    }

    public CustomPet getPet(Player player) {
        return playerPets.get(player.getUniqueId());
    }
}