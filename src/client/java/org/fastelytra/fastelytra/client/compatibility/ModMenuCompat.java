package org.fastelytra.fastelytra.client.compatibility;

import com.google.gson.JsonArray;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import org.fastelytra.fastelytra.client.config.ConfigDefaults;
import org.fastelytra.fastelytra.client.config.ConfigManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.fastelytra.title"));

            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.fastelytra.category.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // "Enable Fast Elytra" toggle
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("config.fastelytra.enableFastElytra"),
                            ConfigManager.getOrSetDefault("enableFastElytra", ConfigDefaults.enableFastElytra, Boolean.class))
                    .setDefaultValue(ConfigDefaults.enableFastElytra)
                    .setTooltip(Text.translatable("config.fastelytra.enableFastElytra.tooltip"))
                    .setSaveConsumer(newValue -> ConfigManager.config.addProperty("enableFastElytra", newValue))
                    .build()
            );

            // "Disable Jump Key Stops Gliding" toggle
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("config.fastelytra.disableJumpKeyStopsGliding"),
                            ConfigManager.getOrSetDefault("disableJumpKeyStopsGliding", ConfigDefaults.disableJumpKeyStopsGliding, Boolean.class))
                    .setDefaultValue(ConfigDefaults.disableJumpKeyStopsGliding)
                    .setTooltip(Text.translatable("config.fastelytra.disableJumpKeyStopsGliding.tooltip"))
                    .setSaveConsumer(newValue -> ConfigManager.config.addProperty("disableJumpKeyStopsGliding", newValue))
                    .build()
            );

            // "Speed Boost Multiplier" slider
            general.addEntry(entryBuilder.startFloatField(
                            Text.translatable("config.fastelytra.speedBoostMultiplier"),
                            ConfigManager.getOrSetDefault("speedBoostMultiplier", ConfigDefaults.speedBoostMultiplier, Double.class).floatValue())
                    .setDefaultValue((float) ConfigDefaults.speedBoostMultiplier)
                    .setMin(0.01f)
                    .setMax(1.0f)
                    .setTooltip(Text.translatable("config.fastelytra.speedBoostMultiplier.tooltip"))
                    .setSaveConsumer(newValue -> ConfigManager.config.addProperty("speedBoostMultiplier", newValue))
                    .build()
            );

            // "Use W Key For Boost" toggle
            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.translatable("config.fastelytra.useWKeyForBoost"),
                            ConfigManager.getOrSetDefault("useWKeyForBoost", ConfigDefaults.useWKeyForBoost, Boolean.class))
                    .setDefaultValue(ConfigDefaults.useWKeyForBoost)
                    .setTooltip(Text.translatable("config.fastelytra.useWKeyForBoost.tooltip"))
                    .setSaveConsumer(newValue -> ConfigManager.config.addProperty("useWKeyForBoost", newValue))
                    .build()
            );

            // Server settings category
            ConfigCategory serverSettings = builder.getOrCreateCategory(Text.translatable("config.fastelytra.category.server"));

            // "Server Mode" dropdown
            serverSettings.addEntry(entryBuilder.startStringDropdownMenu(
                            Text.translatable("config.fastelytra.serverMode"),
                            ConfigManager.getOrSetDefault("serverMode", ConfigDefaults.serverMode, String.class))
                    .setDefaultValue(ConfigDefaults.serverMode)
                    .setTooltip(Text.translatable("config.fastelytra.serverMode.tooltip"))
                    .setSelections(List.of("unrestricted", "whitelist", "blacklist"))
                    .setSaveConsumer(newValue -> ConfigManager.config.addProperty("serverMode", newValue))
                    .build()
            );

            // "Server Whitelist" string list
            List<String> whitelist = new ArrayList<>();
            JsonArray whitelistArray = ConfigManager.getOrSetDefault("serverWhitelist", ConfigDefaults.getDefaultWhitelist(), JsonArray.class);
            if (whitelistArray != null) {
                for (int i = 0; i < whitelistArray.size(); i++) {
                    whitelist.add(whitelistArray.get(i).getAsString());
                }
            }

            serverSettings.addEntry(entryBuilder.startStrList(
                            Text.translatable("config.fastelytra.serverWhitelist"),
                            whitelist)
                    .setDefaultValue(new ArrayList<>())
                    .setTooltip(Text.translatable("config.fastelytra.serverWhitelist.tooltip"))
                    .setSaveConsumer(newList -> {
                        JsonArray newArray = new JsonArray();
                        for (String server : newList) {
                            newArray.add(server);
                        }
                        ConfigManager.config.add("serverWhitelist", newArray);
                    })
                    .build()
            );

            // "Server Blacklist" string list
            List<String> blacklist = new ArrayList<>();
            JsonArray blacklistArray = ConfigManager.getOrSetDefault("serverBlacklist", ConfigDefaults.getDefaultBlacklist(), JsonArray.class);
            if (blacklistArray != null) {
                for (int i = 0; i < blacklistArray.size(); i++) {
                    blacklist.add(blacklistArray.get(i).getAsString());
                }
            }

            serverSettings.addEntry(entryBuilder.startStrList(
                            Text.translatable("config.fastelytra.serverBlacklist"),
                            blacklist)
                    .setDefaultValue(new ArrayList<>())
                    .setTooltip(Text.translatable("config.fastelytra.serverBlacklist.tooltip"))
                    .setSaveConsumer(newList -> {
                        JsonArray newArray = new JsonArray();
                        for (String server : newList) {
                            newArray.add(server);
                        }
                        ConfigManager.config.add("serverBlacklist", newArray);
                    })
                    .build()
            );

            builder.setSavingRunnable(ConfigManager::saveConfig); // Save config when screen closes

            Screen screen = builder.build();
            return screen;
        };
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ModMenuApi.super.getProvidedConfigScreenFactories();
    }
}
