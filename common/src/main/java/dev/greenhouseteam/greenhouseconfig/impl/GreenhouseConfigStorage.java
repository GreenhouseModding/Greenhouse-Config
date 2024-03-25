package dev.greenhouseteam.greenhouseconfig.impl;

import com.google.gson.JsonPrimitive;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCOps;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCElement;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCObject;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.element.JsonCPrimitive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GreenhouseConfigStorage {

    private static final Map<GreenhouseConfigHolder<?>, Object> SERVER_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> CLIENT_CONFIGS = new HashMap<>();

    public static <T> T getServerConfig(GreenhouseConfigHolderImpl<T> holder) {
        if (!SERVER_CONFIGS.containsKey(holder)) {
            throw new UnsupportedOperationException("Could not find server config for config '" + holder.getModId() + "'.");
        }
        return (T) SERVER_CONFIGS.get(holder);
    }

    public static <T> void createServerConfigIfMissing(GreenhouseConfigHolderImpl<T> holder) {
        if (!SERVER_CONFIGS.containsKey(holder)) {
            SERVER_CONFIGS.put(holder, holder.getDefaultServerValue());

            JsonCElement element = holder.encode(JsonCOps.INSTANCE, holder.getDefaultServerValue());

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
            try {
                File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getModId() + ".jsonc").toFile();
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        GreenhouseConfig.LOG.error("Failed to create config for mod '" + holder.getModId() + "' in config directory. Skipping and using default config values.");
                        return;
                    }
                }
                FileWriter writer = new FileWriter(file);
                element.write(writer);
                GreenhouseConfig.LOG.info(element.toString());
            } catch (IOException e) {
                GreenhouseConfig.LOG.error("Failed to write config for mod '" + holder.getModId() + "' to config directory. Skipping and using default config values.");
            }
        }
    }

    public static <T> T getValues(GreenhouseConfigHolder<T> config, ConfigSide side) {
        return (T) (side == ConfigSide.SERVER ? SERVER_CONFIGS.get(config) : CLIENT_CONFIGS.get(config));
    }
}
