package dev.greenhouseteam.greenhouseconfig.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GreenhouseConfigHolderImpl<T> implements GreenhouseConfigHolder<T> {

    private final String configName;
    private final int schemaVersion;
    private final T defaultServerValue;
    private final T defaultClientValue;
    private final Codec<T> serverCodec;
    private final Codec<T> clientCodec;
    private final Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction;
    private final BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback;
    private final Map<Integer, Codec<T>> backwardsCompatCodecsServer;
    private final Map<Integer, Codec<T>> backwardsCompatCodecsClient;

    public GreenhouseConfigHolderImpl(String configName, int schemaVersion,
                                      T defaultServerValue, T defaultClientValue,
                                      Codec<T> serverCodec, Codec<T> clientCodec,
                                      @Nullable Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction,
                                      @Nullable BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback,
                                      Map<Integer, Codec<T>> backwardsCompatCodecsServer,
                                      Map<Integer, Codec<T>> backwardsCompatCodecsClient) {
        this.configName = configName;
        this.schemaVersion = schemaVersion;
        this.defaultServerValue = defaultServerValue;
        this.defaultClientValue = defaultClientValue;
        this.serverCodec = serverCodec;
        this.clientCodec = clientCodec;
        this.networkCodecFunction = networkCodecFunction;
        this.postRegistryPopulationCallback = postRegistryPopulationCallback;
        this.backwardsCompatCodecsServer = backwardsCompatCodecsServer;
        this.backwardsCompatCodecsClient = backwardsCompatCodecsClient;
    }

    @Override
    public String getConfigName() {
        return this.configName;
    }

    @Override
    public T getDefaultValue() {
        return GreenhouseConfig.getPlatform().getSide() == ConfigSide.DEDICATED_SERVER ? defaultServerValue : defaultClientValue;
    }

    public <E> E encode(DynamicOps<E> ops, T value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == ConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
        return codec.encodeStart(ops, value).getPartialOrThrow(s -> new IllegalStateException("Failed to encode config for mod '" + this.configName + "'. " + s));
    }

    public <E> DataResult<Pair<T, E>> decode(DynamicOps<E> ops, E value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == ConfigSide.DEDICATED_SERVER ? serverCodec : clientCodec;
        return codec.decode(ops, value);
    }

    public int getSchemaVersion() {
        return this.schemaVersion;
    }

    public void postRegistryPopulation(HolderLookup.Provider registries, T value) {
        if (postRegistryPopulationCallback == null)
            return;
        postRegistryPopulationCallback.accept(registries, value);
    }

    @Nullable
    public StreamCodec<FriendlyByteBuf, T> getNetworkCodec(T clientConfig) {
        if (networkCodecFunction == null)
            return null;
        return networkCodecFunction.apply(clientConfig);
    }

    public Codec<T> getBackwardsCompatCodec(int configVersion) {
        return GreenhouseConfig.getPlatform().getSide() == ConfigSide.DEDICATED_SERVER ? backwardsCompatCodecsServer.get(configVersion) : backwardsCompatCodecsClient.get(configVersion);
    }

    @Override
    public int hashCode() {
        return configName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GreenhouseConfigHolderImpl<?> otherHolder)) {
            return false;
        }

        return otherHolder.configName.equals(this.configName);
    }
}
