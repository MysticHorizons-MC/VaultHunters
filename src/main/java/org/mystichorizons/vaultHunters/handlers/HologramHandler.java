package org.mystichorizons.vaultHunters.handlers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramHandler {

    private final JavaPlugin plugin;
    private final LangHandler langHandler;
    private boolean useDecentHolograms = false;
    private final Map<Location, Hologram> decentHologramsMap = new HashMap<>();
    private final Map<Location, ArmorStand> internalHologramsMap = new HashMap<>();

    public HologramHandler(JavaPlugin plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        detectHologramPlugins();
    }

    private void detectHologramPlugins() {
        Plugin decentHologramsPlugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");

        if (decentHologramsPlugin != null && decentHologramsPlugin.isEnabled()) {
            useDecentHolograms = true;
            plugin.getLogger().info("DecentHolograms detected and will be used for holograms.");
        } else {
            plugin.getLogger().info("No supported hologram plugins detected. Falling back to internal hologram methods.");
        }
    }

    public void createOrUpdateHologram(Location location, String tierName, String cooldownTime) {
        if (useDecentHolograms) {
            createOrUpdateDecentHologram(location, tierName, cooldownTime);
        } else {
            createOrUpdateInternalHologram(location, tierName, cooldownTime);
        }
    }

    private void createOrUpdateDecentHologram(Location location, String tierName, String cooldownTime) {
        // Sanitize the location to create a valid hologram name
        String sanitizedLocation = sanitizeLocation(location);
        String hologramName = "vault_hologram_" + sanitizedLocation;

        // Check if the hologram already exists and update it
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram == null) {
            hologram = DHAPI.createHologram(hologramName, location.add(0, plugin.getConfig().getDouble("vaults.hologram.above-vault", 1.5), 0));
        }

        // Clear existing lines and add new ones
        DHAPI.updateHologram(String.valueOf(hologram));
        String hoverName = plugin.getConfig().getString("vaults.hologram.hover_name", "%tier_name% Vault").replace("%tier_name%", tierName);
        DHAPI.addHologramLine(hologram, hoverName);

        if (plugin.getConfig().getBoolean("vaults.hologram.show-cooldown", true)) {
            String cooldownMessage = langHandler.formatMessage("vault-hologram.cooldown-format", "time", cooldownTime);
            DHAPI.addHologramLine(hologram, cooldownMessage);
        }

        hologram.showAll();
        decentHologramsMap.put(location, hologram);
    }

    private void createOrUpdateInternalHologram(Location location, String tierName, String cooldownTime) {
        // Retrieve existing hologram or create a new one
        ArmorStand hologram = internalHologramsMap.get(location);
        if (hologram == null) {
            hologram = location.getWorld().spawn(location.add(0, plugin.getConfig().getDouble("vaults.hologram.above-vault", 1.5), 0), ArmorStand.class);
            hologram.setGravity(false);
            hologram.setVisible(false);
            internalHologramsMap.put(location, hologram);
        }

        // Update hologram text
        String hoverName = plugin.getConfig().getString("vaults.hologram.hover_name", "%tier_name% Vault").replace("%tier_name%", tierName);
        hologram.setCustomName(hoverName);
        hologram.setCustomNameVisible(true);

        // Optionally handle cooldown hologram line
        if (plugin.getConfig().getBoolean("vaults.hologram.show-cooldown", true)) {
            ArmorStand cooldownHologram = location.getWorld().spawn(location.add(0, -0.25, 0), ArmorStand.class);
            cooldownHologram.setCustomName(langHandler.formatMessage("vault-hologram.cooldown-format", "time", cooldownTime));
            cooldownHologram.setCustomNameVisible(true);
            cooldownHologram.setGravity(false);
            cooldownHologram.setVisible(false);
            internalHologramsMap.put(location, cooldownHologram);
        }
    }

    private String sanitizeLocation(Location location) {
        return String.format("%s_%d_%d_%d",
                location.getWorld().getName().replaceAll("[^a-zA-Z0-9_-]", ""),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    public void removeHologram(Location location) {
        if (useDecentHolograms) {
            removeDecentHologram(location);
        } else {
            removeInternalHologram(location);
        }
    }

    private void removeDecentHologram(Location location) {
        Hologram hologram = decentHologramsMap.remove(location);
        if (hologram != null) {
            hologram.delete();
        }
    }

    private void removeInternalHologram(Location location) {
        ArmorStand hologram = internalHologramsMap.remove(location);
        if (hologram != null) {
            hologram.remove();
        }
    }

    public boolean isHologramEnabled() {
        return useDecentHolograms || plugin.getConfig().getBoolean("vaults.hologram.enabled", true);
    }
}
