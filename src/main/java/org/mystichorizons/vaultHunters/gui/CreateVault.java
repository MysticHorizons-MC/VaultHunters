package org.mystichorizons.vaultHunters.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.LangHandler;

public class CreateVault {

    private final VaultHunters plugin;
    private final LangHandler langHandler;

    public CreateVault(VaultHunters plugin) {
        this.plugin = plugin;
        this.langHandler = plugin.getLangHandler();
    }

    public void openCreateVaultTier(Player player) {
        // Create an anvil inventory
        Inventory anvil = Bukkit.createInventory(null, InventoryType.ANVIL, langHandler.getMessage("vault-gui"));

        // Create a paper item with the name "Enter the Tier Name"
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        paperMeta.setDisplayName("Enter the Tier Name");
        paper.setItemMeta(paperMeta);

        // Set the items in the anvil slots
        anvil.setItem(0, paper); // First slot

        // Open the anvil inventory for the player
        player.openInventory(anvil);

        // Register the event to handle the anvil slot changes
        registerAnvilPrepareEvent();
    }

    private void registerAnvilPrepareEvent() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPrepareAnvil(PrepareAnvilEvent event) {
                InventoryView view = event.getView();
                ItemStack firstSlotItem = view.getItem(0);

                // Ensure the first slot item is the paper with the default name
                if (firstSlotItem != null && firstSlotItem.getType() == Material.PAPER && firstSlotItem.getItemMeta().hasDisplayName()) {
                    String tierName = firstSlotItem.getItemMeta().getDisplayName();

                    // Create the resulting item with the entered name
                    ItemStack result = new ItemStack(Material.PAPER);
                    ItemMeta resultMeta = result.getItemMeta();
                    resultMeta.setDisplayName(tierName); // Set the display name to the entered name
                    result.setItemMeta(resultMeta);

                    // Set the result slot item to the newly named paper
                    event.setResult(result);
                }
            }
        }, plugin);
    }

    public void handleAnvilClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (inventory.getType() == InventoryType.ANVIL) {
            event.setCancelled(true);

            // Handle the anvil result slot interaction
            if (event.getRawSlot() == 2) { // Result slot is index 2
                ItemStack result = inventory.getItem(2);
                if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                    String newName = result.getItemMeta().getDisplayName();
                    if (plugin.getVaultTiersHandler().vaultTierExists(newName)) {
                        player.sendMessage(langHandler.getMessage("vault-gui-create-exists"));
                        player.closeInventory();
                    } else {
                        plugin.getVaultTiersHandler().createVaultTier(newName);
                        player.sendMessage(langHandler.formatMessage("vault-gui-created-vault", "tier", newName));
                        player.closeInventory();
                        new Editor(plugin).openVaultTierEditor(player, plugin.getVaultTiersHandler().getTier(newName));
                    }
                }
            }
        }
    }
}
