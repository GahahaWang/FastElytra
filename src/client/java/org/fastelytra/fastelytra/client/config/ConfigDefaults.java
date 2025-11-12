package org.fastelytra.fastelytra.client.config;

import com.google.gson.JsonArray;

public class ConfigDefaults {
    public static final boolean enableFastElytra = true;
    public static final boolean disableJumpKeyStopsGliding = false;
    public static final double speedBoostMultiplier = 0.05;
    public static final boolean useWKeyForBoost = false;
    public static final String serverMode = "unrestricted";
    public static final String[] serverWhitelist = new String[]{};
    public static final String[] serverBlacklist = new String[]{};
    
    public static JsonArray getDefaultWhitelist() {
        JsonArray arr = new JsonArray();
        for (String server : serverWhitelist) {
            arr.add(server);
        }
        return arr;
    }
    
    public static JsonArray getDefaultBlacklist() {
        JsonArray arr = new JsonArray();
        for (String server : serverBlacklist) {
            arr.add(server);
        }
        return arr;
    }
}
