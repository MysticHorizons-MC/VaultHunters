package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;
import org.mystichorizons.vaultHunters.handlers.TierItemsHandler;
import org.mystichorizons.vaultHunters.tables.LootItem;

public class ItemEditor extends GUIManager {

    private final LangHandler langHandler;
    private final ItemStack itemToEdit;
    private final String tierName;
    private final int itemIndex;

    public ItemEditor(VaultHunters plugin, ItemStack itemToEdit, String tierName) {
        super(plugin);
        this.langHandler = plugin.getLangHandler();
        this.itemToEdit = itemToEdit;
        this.tierName = tierName;
        this.itemIndex = findItemIndex();
    }

    private int findItemIndex() {
        return plugin.getTierItemsHandler().getLootTableItems().indexOf(
                plugin.getTierItemsHandler().getItem(itemToEdit)
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
        double currentChance = plugin.getTierItemsHandler().getLootTableItems().get(itemIndex).getChance();

        // Create a paper item with the current chance as its display name
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        if (paperMeta != null) {
            paperMeta.setDisplayName(String.valueOf(currentChance));
            paper.setItemMeta(paperMeta);
        }

        // Set the paper item in the first slot of the anvil
        anvil.setItem(0, paper);

        // Register and open the GUI using inherited methods
        registerGUI("ItemEditor", anvil);
        openGUI(player, "ItemEditor");
    }

    @EventHandler
    public void handleAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlot() == 2) {
            ItemStack resultItem = event.getCurrentItem();
            if (resultItem != null && resultItem.getType() == Material.PAPER && resultItem.hasItemMeta()) {
                try {
                    String displayName = resultItem.getItemMeta().getDisplayName();
                    double newChance = Double.parseDouble(displayName);

                    // Update the item's chance in the vault tier
                    LootItem lootItem = plugin.getTierItemsHandler().getLootTableItems().get(itemIndex);
                    plugin.getTierItemsHandler().addLootItem(lootItem.getItem(), newChance, lootItem.getMinQuantity(), lootItem.getMaxQuantity());
                    event.getWhoClicked().sendMessage(langHandler.getMessage("item-editor-save"));
                } catch (NumberFormatException e) {
                    event.getWhoClicked().sendMessage(langHandler.getMessage("item-editor-invalid-chance"));
                }

                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
            }
        }
    }
}
