package dev.greenhouseteam.greenhouseconfig.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.platform.GHConfigIPlatformHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GreenhouseConfigHolderImpl<T> implements GreenhouseConfigHolder<T> {

    private final String modId;
    private final int configVersion;
    private final T defaultServerValue;
    private final T defaultClientValue;
    private final Codec<T> serverCodec;
    private final Codec<T> clientCodec;
    private final Codec<T> networkCodec;
    private final Map<Integer, Codec<T>> backwardsCompatCodecs;

    public GreenhouseConfigHolderImpl(String modId, int configVersion,
                                      T defaultServerValue, T defaultClientValue,
                                      Codec<T> serverCodec, Codec<T> clientCodec,
                                      @Nullable Codec<T> networkCodec,
                                      Map<Integer, Codec<T>> backwardsCompatCodecs) {
        this.modId = modId;
        this.configVersion = configVersion;
        this.defaultServerValue = defaultServerValue;
        this.defaultClientValue = defaultClientValue;
        this.serverCodec = serverCodec;
        this.clientCodec = clientCodec;
        this.networkCodec = networkCodec;
        this.backwardsCompatCodecs = backwardsCompatCodecs;
    }

    public String getModId() {
        return this.modId;
    }

    @Override
    public T getDefaultServerValue() {
        return this.defaultServerValue;
    }

    @Override
    public T getDefaultClientValue() {
        return this.defaultClientValue;
    }

    public <E> E encode(DynamicOps<E> ops, T value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == ConfigSide.SERVER ? this.serverCodec : this.clientCodec;
        return codec.encodeStart(ops, value).getOrThrow(true, s -> GreenhouseConfig.LOG.error("Failed to encode config for mod '" + this.modId + "'. " + s));
    }

    public <E> T decode(DynamicOps<E> ops, E value) {
        Codec<T> codec = GreenhouseConfig.getPlatform().getSide() == ConfigSide.SERVER ? this.serverCodec : this.clientCodec;
        return codec.decode(ops, value).getOrThrow(true, s -> GreenhouseConfig.LOG.error("Failed to decode config for mod '" + this.modId + "'. Please check your config file. " + s)).getFirst();
    }

    public int getConfigVersion() {
        return this.configVersion;
    }

    public Codec<T> getServerCodec() {
        return this.serverCodec;
    }

    public Codec<T> getClientCodec() {
        return this.clientCodec;
    }

    public Codec<T> getNetworkCodec() {
        return this.networkCodec;
    }

    public Codec<T> getBackwardsCompatCodec(int configVersion) {
        return backwardsCompatCodecs.get(configVersion);
    }

    @Override
    public int hashCode() {
        return modId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GreenhouseConfigHolderImpl<?> otherHolder)) {
            return false;
        }

        return otherHolder.modId.equals(this.modId);
    }
}
