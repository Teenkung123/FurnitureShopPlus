package com.Teenkung123.furnitureShopPlus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class MessageLoader {

    private final FurnitureShopPlus plugin;
    private FileConfiguration config;
    private final HashMap<String, String> messages = new HashMap<>();

    public MessageLoader(FurnitureShopPlus plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key));
        }

    }

    public String getMessage(String key) {
        return messages.get(key);
    }

    public void reloadMessages() {
        loadMessages();
    }

    public FileConfiguration getConfig() {
        return config;
    }

}
