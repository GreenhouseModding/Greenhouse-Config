package dev.greenhouseteam.greenhouseconfig.impl;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import dev.greenhouseteam.greenhouseconfig.api.CommentedJson;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCOps;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadServerConfig(holder, registries);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), ConfigSide.SERVER);
        }
    }

    public static void generateClientConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadClientConfig(holder);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), ConfigSide.CLIENT);
        }
    }

    public static void onRegistryPopulation(HolderLookup.Provider registries) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == ConfigSide.SERVER;
        Map<GreenhouseConfigHolder<?>, Object> configs = isServer ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : configs.entrySet()) {
            ((GreenhouseConfigHolderImpl<Object>)entry.getKey()).postRegistryPopulation(registries, entry.getValue());
            GreenhouseConfig.getPlatform().postPopulationEvent((GreenhouseConfigHolder<Object>) entry.getKey(), entry.getValue(), GreenhouseConfig.getPlatform().getSide());
        }
    }

    public static <T> void loadServerConfig(GreenhouseConfigHolderImpl<T> holder, RegistryAccess registries) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getModId() + ".jsonc").toFile();

        if (file.exists()) {
            try {
                T value = holder.decode(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(file)));
                SERVER_CONFIGS.put(holder, value);
                return;
            } catch (FileNotFoundException ex) {
                GreenhouseConfig.LOG.error("Could not read Greenhouse config file '{}'.", file.getPath());
            }
        }

        SERVER_CONFIGS.put(holder, holder.getDefaultServerValue());
        CommentedJson commented = holder.encode(RegistryOps.create(JsonCOps.INSTANCE, registries), holder.getDefaultServerValue());
        createConfigIfMissing(holder, commented);
    }

    public static <T> void loadClientConfig(GreenhouseConfigHolderImpl<T> holder) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getModId() + ".jsonc").toFile();

        if (file.exists()) {
            try {
                T value = holder.decode(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(file)));
                CLIENT_CONFIGS.put(holder, value);
                return;
            } catch (FileNotFoundException ex) {
                GreenhouseConfig.LOG.error("Could not read Greenhouse config file '{}'.", file.getPath());
            }
        }

        CLIENT_CONFIGS.put(holder, holder.getDefaultClientValue());
        CommentedJson commented = holder.encode(JsonCOps.INSTANCE, holder.getDefaultClientValue());
        createConfigIfMissing(holder, commented);
    }

    public static <T> void createConfigIfMissing(GreenhouseConfigHolderImpl<T> holder, CommentedJson element) {
        try {
            File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getModId() + ".jsonc").toFile();
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    GreenhouseConfig.LOG.error("Failed to create config for mod '" + holder.getModId() + "' in config directory. Skipping and using default config values.");
                    return;
                }
            }
            CommentedJson schema = new CommentedJson(new JsonPrimitive(holder.getConfigVersion()),
                    "This is the schema version of the config.",
                    "DO NOT MODIFY THIS FIELD!!!",
                    "Otherwise your config will not update if the schema changes.");
            CommentedJson.Object object = new CommentedJson.Object();
            if (element instanceof CommentedJson.Object obj) {
                object.put("schema_version", schema);
                object.putAll(obj.getMap());
            } else {
                object.put("schema_version", schema);
                object.put("value", element);
            }
            element = object;

            FileWriter writer = new FileWriter(file);
            JsonCWriter jsonWriter = new JsonCWriter(writer);
            jsonWriter.writeJson(element);
            writer.close();
        } catch (IOException e) {
            GreenhouseConfig.LOG.error("Failed to create config for mod '" + holder.getModId() + "' to config directory. Skipping and using default config values.");
        }
    }
}
