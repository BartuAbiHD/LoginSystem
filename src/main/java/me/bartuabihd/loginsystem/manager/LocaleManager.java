package me.bartuabihd.loginsystem.manager;

import me.bartuabihd.loginsystem.LoginSystem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class LocaleManager {

    private final LoginSystem plugin;
    private FileConfiguration messagesConfig;
    private String lang;

    public LocaleManager(LoginSystem plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        this.lang = plugin.getConfig().getString("settings.messagesLanguage", "en").toLowerCase();
        File langFile = new File(plugin.getDataFolder(), "messages/messages_" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + langFile.getName() + ". Trying to download from GitHub...");
            if (!downloadLanguageFile(lang)) {
                plugin.getLogger().severe("The language file couldn't be downloaded from GitHub either! The default language (en) is being used.");
                this.lang = "en"; // Fallback to English
                plugin.getConfig().set("settings.messagesLanguage", "en");
                plugin.saveConfig();
                langFile = new File(plugin.getDataFolder(), "messages/messages_en.yml");
            }
        }

        // Varsayılan dil dosyalarının her zaman var olduğundan emin ol (ilk açılış için)
        saveDefaultLangFile("en");
        saveDefaultLangFile("tr");

        messagesConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private boolean downloadLanguageFile(String langCode) {
        // Not: GitHub repo'nuzun public olması gerekir.
        String urlString = "https://raw.githubusercontent.com/BartuAbiHD/LoginSystem/main/messages/messages_" + langCode + ".yml";
        try {
            URL url = new URL(urlString);
            File targetFile = new File(plugin.getDataFolder(), "messages/messages_" + langCode + ".yml");
            targetFile.getParentFile().mkdirs(); // 'messages' klasörünü oluştur

            try (InputStream in = url.openStream()) {
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info(targetFile.getName() + " has been successfully downloaded.");
                return true;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "The file could not be downloaded from the URL: " + urlString, e.getMessage());
            return false;
        }
    }

    private void saveDefaultLangFile(String langCode) {
        File langFile = new File(plugin.getDataFolder(), "messages/messages_" + langCode + ".yml");
        if (!langFile.exists()) {
            // JAR'ın içindeki resources/messages/ klasöründen kopyalar.
            plugin.saveResource("messages/messages_" + langCode + ".yml", false);
        }
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return message;
    }
}