package org.fastelytra.fastelytra.client.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static final Path CONFIG_PATH = new File("config/fastelytra.json").toPath();
    public static final Gson GSON = new Gson();
    public static JsonObject config;

    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String content = new String(Files.readAllBytes(CONFIG_PATH));
                config = GSON.fromJson(content, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    public static void createDefaultConfig() {
        config = new JsonObject();
        config.addProperty("enableFastElytra", ConfigDefaults.enableFastElytra);
        config.addProperty("disableJumpKeyStopsGliding", ConfigDefaults.disableJumpKeyStopsGliding);
        config.addProperty("speedBoostMultiplier", ConfigDefaults.speedBoostMultiplier);
        config.addProperty("useWKeyForBoost", ConfigDefaults.useWKeyForBoost);
        config.addProperty("serverMode", ConfigDefaults.serverMode);
        
        JsonArray whitelist = new JsonArray();
        for (String server : ConfigDefaults.serverWhitelist) {
            whitelist.add(server);
        }
        config.add("serverWhitelist", whitelist);
        
        JsonArray blacklist = new JsonArray();
        for (String server : ConfigDefaults.serverBlacklist) {
            blacklist.add(server);
        }
        config.add("serverBlacklist", blacklist);

        saveConfig();
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, GSON.toJson(config).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T getOrSetDefault(String key, T defaultValue, Class<T> clazz) {
        if (config == null) {
            return defaultValue;
        }
        JsonElement elem = config.get(key);
        if (elem == null || elem.isJsonNull()) {
            // Set the default value in config and save
            setConfigValue(key, defaultValue);
            saveConfig();
            return defaultValue;
        }
        T ret;
        try {
            // 根據類型自動轉換
            if (clazz == String.class) ret = clazz.cast(elem.getAsString());
            else if (clazz == Integer.class) ret = clazz.cast(elem.getAsInt());
            else if (clazz == Double.class) ret = clazz.cast(elem.getAsDouble());
            else if (clazz == Boolean.class) ret = clazz.cast(elem.getAsBoolean());
            else if (clazz == Long.class) ret = clazz.cast(elem.getAsLong());
            else if (clazz == JsonArray.class) ret = clazz.cast(elem.getAsJsonArray());
            else ret = GSON.fromJson(elem, clazz);
        } catch (Exception e) {
            // If type conversion fails, set and save default value
            setConfigValue(key, defaultValue);
            saveConfig();
            return defaultValue;
        }
        return ret;
    }

    private static void setConfigValue(String key, Object value) {
        switch (value) {
            case String s -> config.addProperty(key, s);
            case Number number -> config.addProperty(key, number);
            case Boolean b -> config.addProperty(key, b);
            case JsonArray jsonElements -> config.add(key, jsonElements);
            case null, default -> config.add(key, GSON.toJsonTree(value));
        }
    }
}
