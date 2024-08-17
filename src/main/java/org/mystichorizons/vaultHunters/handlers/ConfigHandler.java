package org.mystichorizons.vaultHunters.handlers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigHandler {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public ConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&5Vault Hunters&8] &7");
    }

    public String getLang() {
        return config.getString("lang", "en_US");
    }

    public String getCooldownTime() {
        return config.getString("vaults.cooldown-time", "1d");
    }

    public long getCooldownTimeMillis() {
        String cooldownTime = getCooldownTime();
        char suffix = cooldownTime.charAt(cooldownTime.length() - 1);
        long value = Long.parseLong(cooldownTime.substring(0, cooldownTime.length() - 1));
        long milliseconds;

        switch (suffix) {
            case 's':
                milliseconds = value * 1000; // seconds
                break;
            case 'm':
                milliseconds = value * 60 * 1000; // minutes
                break;
            case 'h':
                milliseconds = value * 60 * 60 * 1000; // hours
                break;
            case 'd':
                milliseconds = value * 24 * 60 * 60 * 1000; // days
                break;
            case 'w':
                milliseconds = value * 7 * 24 * 60 * 60 * 1000; // weeks
                break;
            case 'M':
                milliseconds = value * 30 * 24 * 60 * 60 * 1000; // months (approximate)
                break;
            case 'y':
                milliseconds = value * 365 * 24 * 60 * 60 * 1000; // years (approximate)
                break;
            default:
                throw new IllegalArgumentException("Invalid time suffix: " + suffix);
        }

        return milliseconds;
    }

    public String getCooldownType() {
        return config.getString("vaults.cooldown-type", "PER_PLAYER");
    }

    public String getVaultKey() {
        return config.getString("vaults.vault-key", "TRIAL_KEY");
    }

    public boolean isHologramEnabled() {
        return config.getBoolean("vaults.hologram.enabled", true);
    }

    public String getHoverName() {
        return config.getString("vaults.hologram.hover_name", "&8[%tier_name%&8] &7Vault");
    }

    public double getAboveVault() {
        return config.getDouble("vaults.hologram.above-vault", 1.5);
    }

    public boolean isShowCooldown() {
        return config.getBoolean("vaults.hologram.show-cooldown", true);
    }

    public int getLootingRadius() {
        return config.getInt("options.looting-radius", 10);
    }

    public int getLootAmount() {
        return config.getInt("options.loot-amount", 5);
    }

    public boolean isRandomizeLoot() {
        return config.getBoolean("options.randomize-loot", true);
    }

    public boolean isRandomLootTablesEnabled() {
        return config.getBoolean("options.random-loot-tables.enabled", false);
    }

    public List<String> getRandomLootTables() {
        return config.getStringList("options.random-loot-tables.loot-tables");
    }

    public boolean isParticlesEnabled() {
        return config.getBoolean("options.particles.enabled", true);
    }

    public List<String> getParticleList() {
        return config.getStringList("options.particles.praticle-list");
    }

    public int getParticleLoop() {
        return config.getInt("options.particles.loop", 10);
    }

    public int getParticleLoopDelay() {
        return config.getInt("options.particles.loop-delay", 1);
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("options.sound.enabled", true);
    }

    public List<String> getSoundList() {
        return config.getStringList("options.sound.sound-list");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
