package org.mystichorizons.vaultHunters.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.injector.VaultLootInjector;

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
                plugin.getTierItemsHandler(),
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

        if (block != null && isVaultBlock(block)) {
            handleVaultInteraction(player, block);
        }
    }

    private void handleVaultInteraction(Player player, Block block) {
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        String cooldownType = plugin.getConfigHandler().getConfig().getString("vaults.cooldown-type", "PER_PLAYER");

        // First, drop any existing loot from the NBT data
        vaultLootInjector.dropLootFromNBT(block, player);

        if ("PER_PLAYER".equalsIgnoreCase(cooldownType)) {
            handlePlayerCooldown(player, block, playerUUID, currentTime);
        } else if ("GLOBAL".equalsIgnoreCase(cooldownType)) {
            handleGlobalCooldown(player, block, currentTime);
        }
    }

    private void handlePlayerCooldown(Player player, Block block, UUID playerUUID, long currentTime) {
        Map<Block, Long> cooldowns = playerCooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>());
        if (isOnCooldown(cooldowns, block, currentTime)) {
            long remainingTime = getRemainingCooldownTime(cooldowns, block, currentTime);
            player.sendMessage(plugin.getLangHandler().formatMessage("vault-on-cooldown", "time", formatTime(remainingTime)));
        } else if (vaultLootInjector.injectRandomTierLoot(block)) {
            long cooldownTime = plugin.getConfigHandler().getCooldownTimeMillis();
            cooldowns.put(block, currentTime + cooldownTime);
        }
    }

    private void handleGlobalCooldown(Player player, Block block, long currentTime) {
        if (isOnCooldown(globalCooldowns, block, currentTime)) {
            long remainingTime = getRemainingCooldownTime(globalCooldowns, block, currentTime);
            player.sendMessage(plugin.getLangHandler().formatMessage("vault-on-cooldown", "time", formatTime(remainingTime)));
        } else if (vaultLootInjector.injectRandomTierLoot(block)) {
            long cooldownTime = plugin.getConfigHandler().getCooldownTimeMillis();
            globalCooldowns.put(block, currentTime + cooldownTime);
        }
    }

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

    private boolean isVaultBlock(Block block) {
        return block.getType() == Material.VAULT;
    }
}
