package dev.greenhouseteam.greenhouseconfig.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.greenhouseteam.greenhouseconfig.api.CommentedJson;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigSide;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCOps;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCWriter;
import dev.greenhouseteam.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import net.minecraft.core.HolderLookup;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GreenhouseConfigStorage {

    private static final Map<GreenhouseConfigHolder<?>, Object> SERVER_CONFIGS = new HashMap<>();
    private static final Map<GreenhouseConfigHolder<?>, Object> CLIENT_CONFIGS = new HashMap<>();

    public static <T> T getConfig(GreenhouseConfigHolderImpl<T> holder) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && !CLIENT_CONFIGS.containsKey(holder)) {
            throw new UnsupportedOperationException("Could not find config '" + holder.getConfigName() + "'.");
        }
        return isServer ? (T) SERVER_CONFIGS.get(holder) : (T) CLIENT_CONFIGS.get(holder);
    }

    public static Set<GreenhouseConfigHolder<?>> getConfigs() {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        return isServer ? SERVER_CONFIGS.keySet() : CLIENT_CONFIGS.keySet();
    }

    public static <T> void updateConfig(GreenhouseConfigHolder<T> holder, T value) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        if (isServer && !SERVER_CONFIGS.containsKey(holder) || !isServer && !CLIENT_CONFIGS.containsKey(holder))
            throw new UnsupportedOperationException("Can only update config '" + holder.getConfigName() + "' after the initial config loading stage.");

        if (isServer)
            SERVER_CONFIGS.put(holder, value);
        else
            CLIENT_CONFIGS.put(holder, value);
    }

    public static Collection<SyncGreenhouseConfigPacket> createSyncPackets() {
        ImmutableList.Builder<SyncGreenhouseConfigPacket> list = ImmutableList.builder();
        var map = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : map.entrySet()) {
            var networkCodec = ((GreenhouseConfigHolderImpl<Object>)entry.getKey()).getNetworkCodec(entry.getKey().get());
            if (networkCodec != null)
                list.add(new SyncGreenhouseConfigPacket(entry.getKey().getConfigName(), entry.getValue()));
        }
        return list.build();
    }

    public static <T> T reloadConfig(GreenhouseConfigHolderImpl<T> holder, Consumer<String> onError) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + ".jsonc").toFile();
        try {
            var json = JsonParser.parseReader(new FileReader(file));
            var value = holder.decode(JsonOps.INSTANCE, json);
            if (value.isError()) {
                onError.accept(value.error().orElseThrow().message());
                return null;
            }
            if (value.isError() && value.hasResultOrPartial()) {
                createConfig(holder, value.getPartialOrThrow().getFirst(), file);
                onError.accept(value.error().orElseThrow().message());
            }
            if (GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER)
                SERVER_CONFIGS.put(holder, value.getPartialOrThrow().getFirst());
            else
                CLIENT_CONFIGS.put(holder, value.getPartialOrThrow().getFirst());
            return value.getPartialOrThrow().getFirst();
        } catch (Exception ex) {
            onError.accept(ex.toString());
        }
        return null;
    }

    public static void generateServerConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.SERVER_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadConfig(holder, SERVER_CONFIGS::put);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), GreenhouseConfigSide.DEDICATED_SERVER);
        }
    }

    public static void generateClientConfigs() {
        for (GreenhouseConfigHolder<?> config : GreenhouseConfigHolderRegistry.CLIENT_CONFIG_HOLDERS.values()) {
            GreenhouseConfigHolderImpl<Object> holder = (GreenhouseConfigHolderImpl<Object>) config;
            loadConfig(holder, CLIENT_CONFIGS::put);
            GreenhouseConfig.getPlatform().postLoadEvent(holder, holder.get(), GreenhouseConfigSide.CLIENT);
        }
    }

    public static void onRegistryPopulation(HolderLookup.Provider registries) {
        boolean isServer = GreenhouseConfig.getPlatform().getSide() == GreenhouseConfigSide.DEDICATED_SERVER;
        Map<GreenhouseConfigHolder<?>, Object> configs = isServer ? SERVER_CONFIGS : CLIENT_CONFIGS;
        for (Map.Entry<GreenhouseConfigHolder<?>, Object> entry : configs.entrySet()) {
            ((GreenhouseConfigHolderImpl<Object>)entry.getKey()).postRegistryPopulation(registries, entry.getValue());
            GreenhouseConfig.getPlatform().postPopulationEvent((GreenhouseConfigHolder<Object>) entry.getKey(), entry.getValue(), GreenhouseConfig.getPlatform().getSide());
        }
    }

    public static void individualRegistryPopulation(HolderLookup.Provider registries, GreenhouseConfigHolder<?> holder) {
        individualRegistryPopulation(registries, holder, holder.get());
    }

    public static void individualRegistryPopulation(HolderLookup.Provider registries, GreenhouseConfigHolder<?> holder, Object value) {
        ((GreenhouseConfigHolderImpl<Object>)holder).postRegistryPopulation(registries, value);
        GreenhouseConfig.getPlatform().postPopulationEvent((GreenhouseConfigHolder<Object>) holder, value, GreenhouseConfig.getPlatform().getSide());
    }

    private static <T> void loadConfig(GreenhouseConfigHolderImpl<T> holder, BiConsumer<GreenhouseConfigHolder<?>, Object> consumer) {
        File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + ".jsonc").toFile();

        if (file.exists()) {
            try {
                var json = JsonParser.parseReader(new FileReader(file));
                int schemaVersion = readSchemaVersion(file);
                if (schemaVersion != holder.getSchemaVersion()) {
                    Codec<T> oldCodec = holder.getBackwardsCompatCodec(schemaVersion);
                    if (oldCodec != null) {
                        var dataResult = oldCodec.decode(JsonOps.INSTANCE, json);
                        if (!dataResult.hasResultOrPartial()) {
                            GreenhouseConfig.LOG.error("Could not decode old config file '{}'. Using default instead.", file.getPath());
                        } else {
                            T value = createConfig(holder, dataResult.resultOrPartial(GreenhouseConfig.LOG::error).orElseThrow().getFirst(), file);
                            consumer.accept(holder, value);
                            return;
                        }
                    }
                } else {
                    var value = holder.decode(JsonOps.INSTANCE, json);
                    if (value.isError() && value.hasResultOrPartial())
                        createConfig(holder, value.getPartialOrThrow().getFirst(), file);
                    consumer.accept(holder, value.getPartialOrThrow().getFirst());
                    return;
                }
            } catch (Exception ex) {
                GreenhouseConfig.LOG.error("Could not read config file '{}'.", file.getPath(), ex);
            }
        }

        consumer.accept(holder, holder.getDefaultValue());
        createConfig(holder, holder.getDefaultValue());
    }

    public static <T> void createConfig(GreenhouseConfigHolder<T> holder, T config) {
        try {
            int folderCount = holder.getConfigName().split("/").length;

            if (folderCount > 1) {
                String folderName = holder.getConfigName().substring(0, holder.getConfigName().lastIndexOf("/"));
                Path path = GreenhouseConfig.getPlatform().getConfigPath().resolve(folderName);
                Files.createDirectories(path);
            }

            File file = GreenhouseConfig.getPlatform().getConfigPath().resolve(holder.getConfigName() + ".jsonc").toFile();
            if (!file.exists())
                Files.createFile(file.toPath());
            createConfig(holder, config, file);
        } catch (IOException ex) {
            GreenhouseConfig.LOG.error("Failed to create config for mod '{}' to config directory. Skipping and using default config values.", holder.getConfigName(), ex);
        }
    }

    private static <T> T createConfig(GreenhouseConfigHolder<T> holder, T config, File file) throws IOException {
        CommentedJson element = ((GreenhouseConfigHolderImpl<T>)holder).encode(JsonCOps.INSTANCE, config);

        if (!(element instanceof CommentedJson.Object)) {
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
}
