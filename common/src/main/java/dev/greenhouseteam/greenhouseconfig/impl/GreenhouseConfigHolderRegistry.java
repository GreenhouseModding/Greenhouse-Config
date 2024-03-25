package dev.greenhouseteam.greenhouseconfig.impl;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;

import java.util.HashMap;
import java.util.Map;

public class GreenhouseConfigHolderRegistry {
    public static final Map<String, GreenhouseConfigHolder<?>> SERVER_CONFIG_HOLDERS = new HashMap<>();
    public static final Map<String, GreenhouseConfigHolder<?>> CLIENT_CONFIG_HOLDERS = new HashMap<>();

    public static void registerServerConfig(String modId, GreenhouseConfigHolder<?> config) {
        if (SERVER_CONFIG_HOLDERS.containsKey(modId)) {
            throw new IllegalArgumentException("A config for " + modId + " has already been registered on the server.");
        }
        SERVER_CONFIG_HOLDERS.put(modId, config);
    }

    public static void registerClientConfig(String modId, GreenhouseConfigHolder<?> config) {
        if (CLIENT_CONFIG_HOLDERS.containsKey(modId)) {
            throw new IllegalArgumentException("A config for " + modId + " has already been registered on the client.");
        }
        CLIENT_CONFIG_HOLDERS.put(modId, config);
    }
}
