package ru.hogeltbellai.CloudixNetwork.api.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

import java.util.*;

public class MenuAPI implements Listener {

    private static final Map<UUID, CustomMenu> customMenuMap = new HashMap<>();
    private static final Map<ItemStack, ItemAction> itemActions = new HashMap<>();
    private static final Map<UUID, Long> lastClickTimes = new HashMap<>();

    private static final long CLICK_DELAY = 1000;
    private static final Set<Inventory> customInventories = Collections.newSetFromMap(new WeakHashMap<>());

    public static void createMenu(Player player, String title, int size) {
        CustomMenu customMenu = customMenuMap.get(player.getUniqueId());
        Inventory inventory;
        if (customMenu == null || !customMenu.getTitle().equals(title)) {
            inventory = Bukkit.createInventory(player, size, title);
            customMenu = new CustomMenu(title, inventory);
            customMenuMap.put(player.getUniqueId(), customMenu);
            customInventories.add(inventory);
        } else {
            inventory = customMenu.getInventory();
        }

        player.openInventory(inventory);
    }

    public static boolean isCustomInventory(Player player) {
        Inventory playerInventory = player.getOpenInventory().getTopInventory();
        return playerInventory != null && customInventories.contains(playerInventory);
    }

    public static void setMenuItem(Player player, String title, int slot, ItemStack itemStack, Runnable runnable) {
        CustomMenu customMenu = customMenuMap.get(player.getUniqueId());
        if (customMenu != null && customMenu.getTitle().equals(title)) {
            customMenu.setItem(slot, itemStack, runnable);
        }
    }

    public static void clearMenu(Player player, String title) {
        CustomMenu customMenu = customMenuMap.get(player.getUniqueId());
        if (customMenu != null && customMenu.getTitle().equals(title)) {
            customMenu.clearItems();
        }
    }

    public static void setItemAction(ItemStack itemStack, ItemAction action) {
        itemActions.put(itemStack, action);
    }

    public static List<ItemStack> getAllItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (Inventory inventory : customInventories) {
            if (inventory != null) {
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        allItems.add(item);
                    }
                }
            }
        }
        return allItems;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.getHolder() instanceof Player) {
            String inventoryTitle = player.getOpenInventory().getTitle();
            CustomMenu customMenu = customMenuMap.get(player.getUniqueId());

            if (customMenu != null && customMenu.getTitle().equals(inventoryTitle)) {
                event.setCancelled(true);
                if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
                    int slot = event.getRawSlot();
                    customMenu.handleClick(slot, player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null) {
            for (Map.Entry<ItemStack, ItemAction> entry : itemActions.entrySet()) {
                if (itemInHand.isSimilar(entry.getKey())) {
                    long currentTime = System.currentTimeMillis();
                    long lastClickTime = lastClickTimes.getOrDefault(player.getUniqueId(), 0L);

                    if (currentTime - lastClickTime >= CLICK_DELAY) {
                        lastClickTimes.put(player.getUniqueId(), currentTime);

                        entry.getValue().run(player);
                        event.setCancelled(true);
                    }
                    break;
                }
            }
        }
    }

    @Setter
    @Getter
    private static class CustomMenu {
        private String title;
        private Inventory inventory;
        private Map<Integer, Runnable> integerRunnableMap;

        public CustomMenu(String title, Inventory inventory) {
            this.title = title;
            this.inventory = inventory;
            this.integerRunnableMap = new HashMap<>();
        }

        public void setItem(int slot, ItemStack item, Runnable action) {
            inventory.setItem(slot, item);
            integerRunnableMap.put(slot, action);
        }

        public void handleClick(int slot, Player player) {
            Runnable action = integerRunnableMap.get(slot);
            if (action != null) {
                action.run();
            }
        }

        public void clearItems() {
            inventory.clear();
            integerRunnableMap.clear();
        }
    }

    @FunctionalInterface
    public interface ItemAction {
        void run(Player player);
    }
}