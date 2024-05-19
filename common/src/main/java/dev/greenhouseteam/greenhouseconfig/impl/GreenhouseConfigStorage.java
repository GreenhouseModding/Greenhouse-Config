package dev.greenhouseteam.greenhouseconfig.impl;

import com.google.gson.JsonPrimitive;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCOps;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCElement;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCObject;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCPrimitive;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GreenhouseConfigStorage {

    private static final Map<GreenhouseConfigHolder<?>, Object> SERVER_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> CLIENT_CONFIGS = new HashMap<>();

    public static <T> T getConfig(GreenhouseConfigHolderImpl<T> holder) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == ConfigSide.SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && !CLIENT_CONFIGS.containsKey(holder)) {
            throw new UnsupportedOperationException("Could not find config '" + holder.getModId() + "'.");
        }
        return isServer ? (T) SERVER_CONFIGS.get(holder) : (T) CLIENT_CONFIGS.get(holder);
    }

    public static void generateServerConfigs(RegistryAccess registries) {
        for (Object config : GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.values())
            createServerConfigIfMissing((GreenhouseConfigHolderImpl<?>) config, registries);
    }

    public static void generateClientConfigs() {
        for (Object config : GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.values())
            createClientConfigIfMissing((GreenhouseConfigHolderImpl<?>) config);
    }

    public static void onRegistryPopulation(HolderLookup.Provider registries) {
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : CLIENT_CONFIGS.entrySet())
            ((GreenhouseConfigHolderImpl<Object>)entry.getKey()).postRegistryPopulation(registries, entry.getValue());
    }

    public static <T> void createServerConfigIfMissing(GreenhouseConfigHolderImpl<T> holder, RegistryAccess registries) {
        if (!SERVER_CONFIGS.containsKey(holder)) {
            SERVER_CONFIGS.put(holder, holder.getDefaultServerValue());
            createConfigIfMissing(holder, holder.encode(RegistryOps.create(JsonCOps.INSTANCE, registries), holder.getDefaultServerValue()));
        }
    }

    public static <T> void createClientConfigIfMissing(GreenhouseConfigHolderImpl<T> holder) {
        if (!CLIENT_CONFIGS.containsKey(holder)) {
            CLIENT_CONFIGS.put(holder, holder.getDefaultClientValue());
            createConfigIfMissing(holder, holder.encode(JsonCOps.INSTANCE, holder.getDefaultClientValue()));
        }
    }

    public static <T> void createConfigIfMissing(GreenhouseConfigHolderImpl<T> holder, JsonCElement element) {
        try {
            File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getModId() + ".jsonc").toFile();
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    GreenhouseConfig.LOG.error("Failed to create config for mod '" + holder.getModId() + "' in config directory. Skipping and using default config values.");
                    return;
                }
            }
            JsonCPrimitive schema = new JsonCPrimitive(new JsonPrimitive(holder.getConfigVersion()));
            schema.addComment("This is the schema version of the config.");
            schema.addComment("DO NOT MODIFY THIS FIELD!!!");
            schema.addComment("Otherwise your config will not update if the schema changes.");

            if (element instanceof JsonCObject jsonCObject) {
                jsonCObject.addElementToBeginning("schema_version", schema);
            } else {
                JsonCObject object = new JsonCObject();
                object.addElement("schema_version", schema);
                object.addElement("value", element);
                element = object;
            }

            FileWriter writer = new FileWriter(file);
            element.write(writer);
        } catch (IOException e) {
            GreenhouseConfig.LOG.error("Failed to create config for mod '" + holder.getModId() + "' to config directory. Skipping and using default config values.");
        }
    }
}
