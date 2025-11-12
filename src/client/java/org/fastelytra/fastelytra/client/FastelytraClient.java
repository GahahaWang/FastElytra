package org.fastelytra.fastelytra.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import com.google.gson.JsonArray;

public class FastelytraClient implements ClientModInitializer {

    public boolean jumpKeyPreviouslyPressed = false; // Track the state of the jump key
    public KeyBinding boostKey;
    public KeyBinding continuousBoostKey;
    public boolean isContinuousBoost = false;
    public ArrayList<KeyBinding> rotate = new ArrayList<>(4);
    private static final KeyBinding.Category FAST_ELYTRA_CATEGORY = KeyBinding.Category.create(Identifier.of("fastelytra", "main"));

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();

        // Register custom keybind
        boostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.boost", // Translation key
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_B,
                FAST_ELYTRA_CATEGORY
        ));

        continuousBoostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.continuousBoost",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.fastelytra"
        ));

        rotate.add(KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.rotate.yaw_l",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                "category.fastelytra"
        )));
        rotate.add(KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.rotate.yaw_r",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                "category.fastelytra"
        )));
        rotate.add(KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.rotate.pitch_u",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "category.fastelytra"
        )));
        rotate.add(KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fastelytra.rotate.pitch_d",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                "category.fastelytra"
        )));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                PlayerEntity player = client.player;
                var yaw = player.getYaw();
                var pitch = player.getPitch();
                if (rotate.get(0).isPressed()) player.setYaw(yaw-0.1f);
                if (rotate.get(1).isPressed()) player.setYaw(yaw+0.1f);
                if (rotate.get(2).isPressed()) player.setPitch(pitch-0.1f);
                if (rotate.get(3).isPressed()) player.setPitch(pitch+0.1f);
            }
        });

        // Reset continuous boost on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isContinuousBoost = false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                PlayerEntity player = client.player;

                // Check if mod functions are allowed on servers
                if (!isServerAllowed(client)) {
                    return; // Disable mod functions on servers if not allowed
                }
                if (!player.isGliding()) isContinuousBoost = false;
                // Check if continuous boost key is pressed
                if (continuousBoostKey.wasPressed()) {
                    if (!player.isGliding()) {client.player.sendMessage(Text.literal("not gliding"), false); return;}
                    isContinuousBoost = !isContinuousBoost;
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                PlayerEntity player = client.player;
                MinecraftClient minecraftClient = MinecraftClient.getInstance();

                // Check if mod functions are allowed on servers
                if (!isServerAllowed(client)) {
                    return; // Disable mod functions on servers if not allowed
                }

                // Fast Elytra functionality
                if (ConfigManager.getOrSetDefault("enableFastElytra", ConfigDefaults.enableFastElytra, Boolean.class)) {
                    boolean useWKey = ConfigManager.getOrSetDefault("useWKeyForBoost", ConfigDefaults.useWKeyForBoost, Boolean.class);
                    boolean isBoostKeyPressed = boostKey.isPressed();

                    if (player.isGliding() && (useWKey && minecraftClient.options.forwardKey.isPressed() || isBoostKeyPressed || isContinuousBoost)) {
                        double speedBoost = ConfigManager.getOrSetDefault("speedBoostMultiplier", ConfigDefaults.speedBoostMultiplier, Double.class);
                        player.addVelocity(
                                player.getRotationVector().x * speedBoost,
                                player.getRotationVector().y * speedBoost,
                                player.getRotationVector().z * speedBoost
                        );
                    }
                }

                // Jump key stops gliding functionality
                if (!ConfigManager.getOrSetDefault("disableJumpKeyStopsGliding", ConfigDefaults.disableJumpKeyStopsGliding, Boolean.class)) {
                    boolean jumpKeyPressed = minecraftClient.options.jumpKey.isPressed();

                    if (player.isGliding() && jumpKeyPressed && !jumpKeyPreviouslyPressed) {
                        player.stopGliding();
                    }

                    jumpKeyPreviouslyPressed = jumpKeyPressed;
                }
            }
        });
    }

    // Check if mod functions are allowed on the current server
    private boolean isServerAllowed(MinecraftClient client) {
        if (client.getCurrentServerEntry() == null) {
            return true; // Always allow in singleplayer
        }

        String serverAddress = client.getCurrentServerEntry().address;
        String serverMode = ConfigManager.getOrSetDefault("serverMode", ConfigDefaults.serverMode, String.class);

        switch (serverMode) {
            case "unrestricted":
                return true; // Allow on all servers
            case "whitelist":
                // Only allow if server is in whitelist
                JsonArray whitelist = ConfigManager.getOrSetDefault("serverWhitelist", ConfigDefaults.getDefaultWhitelist(), JsonArray.class);
                for (int i = 0; i < whitelist.size(); i++) {
                    if (whitelist.get(i).getAsString().equalsIgnoreCase(serverAddress)) {
                        return true;
                    }
                }
                return false;
            case "blacklist":
                // Allow unless server is in blacklist
                JsonArray blacklist = ConfigManager.getOrSetDefault("serverBlacklist", ConfigDefaults.getDefaultBlacklist(), JsonArray.class);
                for (int i = 0; i < blacklist.size(); i++) {
                    if (blacklist.get(i).getAsString().equalsIgnoreCase(serverAddress)) {
                        return false;
                    }
                }
                return true;
            default:
                return false; // Default to disabled if mode is unknown
        }
    }
}
