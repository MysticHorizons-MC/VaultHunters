package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;
import org.mystichorizons.vaultHunters.handlers.VaultTiersHandler;

import java.io.File;

public class Editor extends GUIManager {

    private final LangHandler langHandler;

    public Editor(VaultHunters plugin) {
        super(plugin);
        this.langHandler = plugin.getLangHandler();
    }

    public void openVaultTierEditor(Player player, VaultTiersHandler.VaultTier tier) {
        Inventory editorMenu = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Edit Vault Tier: " + tier.getName());

        ItemStack glassPane = new GUI(plugin).createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                editorMenu.setItem(i, glassPane);
            }
        }

        editorMenu.setItem(45, new GUI(plugin).createItem(Material.ARROW, langHandler.getMessage("vault-gui-back")));
        editorMenu.setItem(46, new GUI(plugin).createItem(Material.GREEN_WOOL, langHandler.getMessage("vault-gui-save")));
        editorMenu.setItem(47, new GUI(plugin).createItem(Material.BARRIER, langHandler.getMessage("vault-gui-cancel")));
        editorMenu.setItem(48, new GUI(plugin).createItem(Material.NAME_TAG, langHandler.getMessage("vault-gui-edit-hologram")));
        editorMenu.setItem(49, new GUI(plugin).createItem(Material.CHEST, langHandler.getMessage("vault-gui-add-loot")));

        // Register and open the GUI using inherited methods
        registerGUI("VaultTierEditor", editorMenu);
        openGUI(player, "VaultTierEditor");
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event, String tierName) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        switch (clickedItem.getType()) {
            case ARROW:
                new GUI(plugin).openMainMenu(player);
                break;

            case GREEN_WOOL:
                plugin.getVaultTiersHandler().saveTier(tierName);
                player.sendMessage(langHandler.getMessage("vault-gui-save"));
                new GUI(plugin).openMainMenu(player);
                break;

            case BARRIER:
                handleBarrierClick(player, tierName);
                break;

            case NAME_TAG:
                // Logic to edit hologram
                break;

            case CHEST:
                new LootEditor(plugin).openLootEditor(player, tierName);
                break;

            default:
                break;
        }
    }

    private void handleBarrierClick(Player player, String tierName) {
        VaultTiersHandler vaultTiersHandler = plugin.getVaultTiersHandler();


        if (!vaultTiersHandler.isTierSaved(tierName)) {
            vaultTiersHandler.deleteTier(tierName);
            player.sendMessage(langHandler.getMessage("vault-gui-cancel-creation"));
        } else {
            vaultTiersHandler.deleteTier(tierName);
            File tierFile = new File(plugin.getDataFolder(), "items/" + tierName + ".yml");
            if (tierFile.exists()) {
                tierFile.delete();
            }
            player.sendMessage(langHandler.getMessage("vault-gui-delete-tier"));
        }

        new GUI(plugin).openMainMenu(player);
    }
}
