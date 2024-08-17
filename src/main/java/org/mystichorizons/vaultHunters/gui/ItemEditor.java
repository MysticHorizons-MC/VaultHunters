package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;

public class ItemEditor {

    private final VaultHunters plugin;
    private final LangHandler langHandler;
    private final ItemStack itemToEdit;
    private final String tierName;
    private final int itemIndex;

    public ItemEditor(VaultHunters plugin, ItemStack itemToEdit, String tierName) {
        this.plugin = plugin;
        this.langHandler = plugin.getLangHandler();
        this.itemToEdit = itemToEdit;
        this.tierName = tierName;
        this.itemIndex = plugin.getTierItemsHandler().getTierItems(tierName).indexOf(
                plugin.getTierItemsHandler().getTierItems(tierName).stream()
                        .filter(tierItem -> tierItem.getItemStack().isSimilar(itemToEdit))
                        .findFirst()
                        .orElse(null)
        );
    }

    public void openItemEditor(Player player) {
        if (itemIndex == -1) {
            player.sendMessage(langHandler.getMessage("item-editor-item-not-found"));
            return;
        }

        // Create an anvil inventory
        Inventory anvil = Bukkit.createInventory(null, InventoryType.ANVIL, langHandler.getMessage("item-editor-title"));

        // Get the current chance of the item in the vault tier
        double currentChance = plugin.getTierItemsHandler().getTierItems(tierName).get(itemIndex).getChance();

        // Create a paper item with the current chance as its display name
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        if (paperMeta != null) {
            paperMeta.setDisplayName(String.valueOf(currentChance));
            paper.setItemMeta(paperMeta);
        }

        // Set the paper item in the first slot of the anvil
        anvil.setItem(0, paper);

        // Register the GUI with the GUIManager for handling
        plugin.getGUIManager().registerGUI("ItemEditor", anvil);

        // Open the anvil inventory for the player
        player.openInventory(anvil);
    }

    public void handleAnvilClick(InventoryClickEvent event) {
        // Check if the inventory clicked is an anvil
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            // Check if the click is on the result slot (index 2)
            if (event.getSlot() == 2) {
                ItemStack resultItem = event.getCurrentItem();
                if (resultItem != null && resultItem.getType() == Material.PAPER && resultItem.hasItemMeta()) {
                    try {
                        String displayName = resultItem.getItemMeta().getDisplayName();
                        if (displayName != null) {
                            double newChance = Double.parseDouble(displayName);

                            // Update the item's chance in the vault tier
                            plugin.getTierItemsHandler().editTierItem(tierName, itemIndex, itemToEdit, newChance);
                            event.getWhoClicked().sendMessage(langHandler.getMessage("item-editor-save"));
                        } else {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        event.getWhoClicked().sendMessage(langHandler.getMessage("item-editor-invalid-chance"));
                    }

                    // Cancel the event and close the inventory after saving
                    event.setCancelled(true);
                    event.getWhoClicked().closeInventory();
                }
            }
        }
    }
}
