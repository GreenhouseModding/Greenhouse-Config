package dev.greenhouseteam.greenhouseconfig.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.greenhouseteam.greenhouseconfig.api.CommentedJson;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCOps;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// TODO: Handle backwards compat with config files.
public class GreenhouseConfigStorage {

    private static final Map<GreenhouseConfigHolder<?>, Object> SERVER_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> CLIENT_CONFIGS = new HashMap<>();

    public static <T> T getConfig(GreenhouseConfigHolderImpl<T> holder) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == ConfigSide.SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && !CLIENT_CONFIGS.containsKey(holder)) {
            throw new UnsupportedOperationException("Could not find config '" + holder.getConfigName() + "'.");
        }
        return isServer ? (T) SERVER_CONFIGS.get(holder) : (T) CLIENT_CONFIGS.get(holder);
    }

    public static void generateServerConfigs(RegistryAccess registries) {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadConfig(holder, registries, SERVER_CONFIGS::put);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), ConfigSide.SERVER);
        }
    }

    public static void generateClientConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadConfig(holder, null, CLIENT_CONFIGS::put);
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

    public static <T> void loadConfig(GreenhouseConfigHolderImpl<T> holder, @Nullable RegistryAccess registries, BiConsumer<GreenhouseConfigHolder<?>, Object> consumer) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + ".jsonc").toFile();
        DynamicOps<CommentedJson> ops = registries != null ? RegistryOps.create(JsonCOps.INSTANCE, registries) : JsonCOps.INSTANCE;

        if (file.exists()) {
            DynamicOps<JsonElement> jsonOps = registries != null ? RegistryOps.create(JsonOps.INSTANCE, registries) : JsonOps.INSTANCE;
            try {
                var json = JsonParser.parseReader(new FileReader(file));
                int schemaVersion = readSchemaVersion(file);
                if (schemaVersion != holder.getSchemaVersion()) {
                    Codec<T> oldCodec = holder.getBackwardsCompatCodec(schemaVersion);
                    if (oldCodec != null) {
                        var dataResult = oldCodec.decode(jsonOps, json);
                        if (dataResult.isError()) {
                            GreenhouseConfig.LOG.error("Could not decode old config file '{}'. Using default instead.", file.getPath());
                        } else {
                            T value = updateConfig(holder, dataResult.getOrThrow().getFirst(), file, ops);
                            consumer.accept(holder, value);
                            return;
                        }
                    }
                } else {
                    T value = holder.decode(jsonOps, json);
                    consumer.accept(holder, value);
                    return;
                }
            } catch (Exception ex) {
                GreenhouseConfig.LOG.error("Could not read config file '{}'.", file.getPath(), ex);
            }
        }

        consumer.accept(holder, holder.getDefaultValue());
        CommentedJson commented = holder.encode(JsonCOps.INSTANCE, holder.getDefaultValue());
        createConfigIfMissing(holder, holder.getDefaultValue(), ops);
    }

    public static <T> T updateConfig(GreenhouseConfigHolderImpl<T> holder, T config, File file, DynamicOps<CommentedJson> ops) throws IOException {
        CommentedJson element = holder.encode(ops, config);

        if (!(element instanceof CommentedJson.Object obj)) {
            CommentedJson.Object object = new CommentedJson.Object();
            object.put("value", element);
            element = object;
        }

        FileWriter writer = new FileWriter(file);
        JsonCWriter jsonWriter = new JsonCWriter(writer);
        jsonWriter.writeJson(element);
        writer.close();
        writeSchemaVersion(file, holder);

        return config;
    }

    public static <T> void createConfigIfMissing(GreenhouseConfigHolderImpl<T> holder, T config, DynamicOps<CommentedJson> ops) {
        try {
            File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + ".jsonc").toFile();
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    GreenhouseConfig.LOG.error("Failed to create config file for mod '{}' in config directory. Skipping and using default config values.", holder.getConfigName());
                    return;
                }
            }
            updateConfig(holder, config, file, ops);
        } catch (IOException ex) {
            GreenhouseConfig.LOG.error("Failed to create config for mod '{}' to config directory. Skipping and using default config values.", holder.getConfigName(), ex);
        }
    }

    private static void writeSchemaVersion(File file, GreenhouseConfigHolder<?> holder) throws IOException {
        Files.setAttribute(file.toPath(), "user:GreenhouseConfigSchemaVersion", ByteBuffer.wrap(String.valueOf(holder.getSchemaVersion()).getBytes(StandardCharsets.UTF_8)));
    }

    private static int readSchemaVersion(File file) throws IOException {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
        ByteBuffer buffer = ByteBuffer.allocate(view.size("GreenhouseConfigSchemaVersion"));
        view.read("GreenhouseConfigSchemaVersion", buffer);
        buffer.flip();
        return Integer.parseInt(StandardCharsets.UTF_8.decode(buffer).toString());
    }

    private static void logSchema(File file, GreenhouseConfigHolder<?> holder) {
        try {
            if (Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class).list().contains("GreenhouseConfigSchemaVersion"))
                GreenhouseConfig.LOG.debug("Config '{}'s schema version is {}.", holder.getConfigName(), readSchemaVersion(file));
        } catch (NoSuchFileException ex) {
            GreenhouseConfig.LOG.debug("Config '{}' does not exist, creating it now.", holder.getConfigName());
        } catch (Exception ex) {
            GreenhouseConfig.LOG.debug("", ex);
        }
    }
}
