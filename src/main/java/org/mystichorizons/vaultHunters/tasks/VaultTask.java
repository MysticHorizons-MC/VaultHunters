package org.mystichorizons.vaultHunters.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mystichorizons.vaultHunters.VaultHunters;
import org.mystichorizons.vaultHunters.handlers.HologramHandler;
import org.mystichorizons.vaultHunters.handlers.ParticleHandler;
import org.mystichorizons.vaultHunters.injector.VaultLootInjector;
import org.mystichorizons.vaultHunters.tables.VaultBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VaultTask implements Listener {

    private final VaultHunters plugin;
    private final ParticleHandler particleHandler;
    private final VaultLootInjector vaultLootInjector;
    private final HologramHandler hologramHandler;
    private final int rangeSquared;

    public VaultTask(VaultHunters plugin, ParticleHandler particleHandler, VaultLootInjector vaultLootInjector, HologramHandler hologramHandler) {
        this.plugin = plugin;
        this.particleHandler = particleHandler;
        this.vaultLootInjector = vaultLootInjector;
        this.hologramHandler = hologramHandler;
        this.rangeSquared = plugin.getServer().getViewDistance() * plugin.getServer().getViewDistance() * 256; // Blocks squared
    }

    public void startMonitoring() {
        plugin.getLogger().info("Starting VaultTask...");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndUpdateVaultsForPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Runs every second (20 ticks) on the main thread
    }

    private void checkAndUpdateVaultsForPlayer(Player player) {
        plugin.getLogger().info("[ADMIN] Checking and updating vaults for player " + player.getName());
        Location playerLocation = player.getLocation();
        ItemStack keyItem = player.getInventory().getItemInMainHand(); // Get the item in the player's hand

        for (Block block : getNearbyVaultBlocks(playerLocation)) {
            if (block != null && block.getType() == Material.VAULT) {
                VaultBlock vaultBlock = new VaultBlock(block); // Create a VaultBlock instance

                // Inject loot using the VaultLootInjector on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean lootInjected = vaultLootInjector.injectRandomTierLoot(vaultBlock, player);
                    plugin.getLogger().info("[ADMIN] Player " + player.getName() + " interacted with a vault from VaultTask.");

                    if (lootInjected) {
                        particleHandler.playParticles(block.getLocation()); // Trigger particles
                        hologramHandler.createOrUpdateHologram(block.getLocation(), "TierName", "CooldownTime"); // Update hologram
                    }
                });
            }
        }
    }

    private Set<Block> getNearbyVaultBlocks(Location location) {
        plugin.getLogger().info("[ADMIN] Getting nearby vault blocks for location " + location.toString());
        Set<Block> vaultBlocks = new HashSet<>();
        int radius = (int) Math.sqrt(rangeSquared);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    if (block.getType() == Material.VAULT) {
                        vaultBlocks.add(block);
                        plugin.getLogger().info("[ADMIN] Found vault block at " + block.getLocation().toString());
                    }
                }
            }
        }
        return vaultBlocks;
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        plugin.getLogger().info("[ADMIN] Handling block break event...");
        List<Block> vaultBlocks = new ArrayList<>(getNearbyVaultBlocks(event.getBlock().getLocation()) );
        Block block = event.getBlock();
        if (block != null && block.getType() == Material.VAULT) {
            // Remove hologram when vault block is broken
            hologramHandler.removeHologram(block.getLocation());
            vaultBlocks.remove(block);
            plugin.getLogger().info("[ADMIN] Removed hologram for vault block at " + block.getLocation().toString());
        }
    }
}
