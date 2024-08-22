package org.mystichorizons.vaultHunters.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.injector.VaultLootInjector;
import org.mystichorizons.vaultHunters.tables.VaultBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaultListener implements Listener {

    private final VaultHunters plugin;
    private final VaultLootInjector vaultLootInjector;
    private final Map<UUID, Map<Block, Long>> playerCooldowns;
    private final Map<Block, Long> globalCooldowns;

    public VaultListener(VaultHunters plugin) {
        this.plugin = plugin;
        this.vaultLootInjector = new VaultLootInjector(plugin,
                plugin.getVaultTiersHandler(),
                plugin.getParticleHandler(),
                plugin.getHologramHandler());
        this.playerCooldowns = new HashMap<>();
        this.globalCooldowns = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVaultInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            handleVaultInteraction(player, block);
        }
    }

    private void handleVaultInteraction(Player player, Block block) {
        VaultBlock vaultBlock = new VaultBlock(block); // Create a VaultBlock instance
        ItemStack keyItem = player.getInventory().getItemInMainHand(); // Get the item in the player's hand

        // Check if the key is valid using the VaultBlock class
        if (vaultBlock.isValidKey(keyItem, Material.TRIAL_KEY, Material.OMINOUS_TRIAL_KEY)) {
            plugin.getLogger().info("[ADMIN] Player " + player.getName() + " interacted with a vault.");
            // If the key is valid, proceed to inject loot and handle the interaction
            boolean lootInjected = vaultLootInjector.injectRandomTierLoot(vaultBlock, player);

            if (lootInjected) {
                plugin.getLogger().info("[ADMIN] Loot injected into the vault.");
                handleCooldown(player, block);
                plugin.getLogger().info("[ADMIN] Cooldown handled.");
            } else {
                player.sendMessage("Failed to inject loot into the vault.");
                plugin.getLogger().info("[ADMIN] Failed to inject loot into the vault.");
            }
        } else {
            // Handle invalid key interaction
            player.sendMessage("This key is not valid for the vault!");
        }
    }

    private void handleCooldown(Player player, Block block) {
        plugin.getLogger().info("[ADMIN] Handling cooldown...");
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        String cooldownType = plugin.getConfigHandler().getConfig().getString("vaults.cooldown-type", "PER_PLAYER");
        plugin.getLogger().info("[ADMIN] Cooldown type: " + cooldownType);

        Map<Block, Long> cooldowns = "PER_PLAYER".equalsIgnoreCase(cooldownType)
                ? playerCooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>())
                : globalCooldowns;

        long cooldownTime = plugin.getConfigHandler().getCooldownTimeMillis();
        cooldowns.put(block, currentTime + cooldownTime);
        plugin.getLogger().info("[ADMIN] Cooldown set for " + cooldownTime + "ms.");
    }

    // TODO: Methods below are to be removed.

    private boolean isOnCooldown(Map<Block, Long> cooldowns, Block block, long currentTime) {
        return cooldowns.getOrDefault(block, 0L) > currentTime;
    }

    private long getRemainingCooldownTime(Map<Block, Long> cooldowns, Block block, long currentTime) {
        return cooldowns.get(block) - currentTime;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
