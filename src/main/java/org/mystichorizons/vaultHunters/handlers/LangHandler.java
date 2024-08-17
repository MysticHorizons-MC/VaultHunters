package org.mystichorizons.vaultHunters.handlers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private YamlConfiguration langConfig;
    private String currentLang;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&(#\\w{6})");

    public LangHandler(JavaPlugin plugin, ConfigHandler configHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.currentLang = configHandler.getLang();
        loadLangFile();
    }

    private void loadLangFile() {
        File langFile = new File(plugin.getDataFolder(), "lang/" + currentLang + ".yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang/" + currentLang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        String prefix = configHandler.getPrefix();
        String message = langConfig.getString(key, "Message not found: " + key);
        if (!message.startsWith(prefix)) {
            message = prefix + message;
        }
        return applyColor(message);
    }

    public String formatMessage(String key, Object... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = "%" + placeholders[i] + "%";
            String value = placeholders[i + 1].toString();
            message = message.replace(placeholder, value);
        }
        return applyColor(message);
    }

    // Reload the language file if necessary
    public void reloadLang() {
        currentLang = configHandler.getLang();
        loadLangFile();
    }

    // Allows dynamic switching of the language file
    public void switchLang(String lang) {
        this.currentLang = lang;
        loadLangFile();
    }

    // Get the current language in use
    public String getCurrentLang() {
        return currentLang;
    }

    // Apply color codes and hex colors to the message
    private String applyColor(String message) {
        // Replace '&' color codes with ChatColor equivalents
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Handle hex color codes
        Matcher matcher = HEX_COLOR_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.valueOf(hexColor) + "");
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}
