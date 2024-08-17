package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mystichorizons.vaultHunters.VaultHunters;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {

    private final VaultHunters plugin;
    private final Map<String, Inventory> registeredGUIs;

    public GUIManager(VaultHunters plugin) {
        this.plugin = plugin;
        this.registeredGUIs = new HashMap<>();
    }

    // Register an existing GUI
    public void registerGUI(String guiName, Inventory gui) {
        registeredGUIs.put(guiName, gui);
    }

    // Create and register a new GUI
    public void createGUI(String guiName, String title, int size) {
        Inventory gui = Bukkit.createInventory(null, size, title);
        registeredGUIs.put(guiName, gui);
    }

    // Open a registered GUI for a player
    public void openGUI(Player player, String guiName) {
        Inventory gui = registeredGUIs.get(guiName);
        if (gui != null) {
            player.openInventory(gui);
        } else {
            plugin.getLogger().warning("GUI with name " + guiName + " does not exist.");
        }
    }

    // Set an item in a specific slot in a GUI
    public void setItem(String guiName, int slot, ItemStack item) {
        Inventory gui = registeredGUIs.get(guiName);
        if (gui != null) {
            gui.setItem(slot, item);
        } else {
            plugin.getLogger().warning("GUI with name " + guiName + " does not exist.");
        }
    }

    // Handle inventory click events
    public void handleInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.getHolder() == null) {
            event.setCancelled(true); // Cancel the event to prevent taking items out of the custom GUI
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            // Handle the click event based on the GUI name and slot
            String guiName = getGUIName(clickedInventory);
            if (guiName != null) {
                plugin.getLogger().info(player.getName() + " clicked on slot " + slot + " in GUI " + guiName);
                // Add your custom logic here
            }
        }
    }

    // Get the GUI name by comparing inventories
    private String getGUIName(Inventory inventory) {
        for (Map.Entry<String, Inventory> entry : registeredGUIs.entrySet()) {
            if (entry.getValue().equals(inventory)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Unregister a GUI
    public void unregisterGUI(String guiName) {
        registeredGUIs.remove(guiName);
    }
}
