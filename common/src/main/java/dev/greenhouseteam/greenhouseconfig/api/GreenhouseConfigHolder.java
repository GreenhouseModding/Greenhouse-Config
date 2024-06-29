package dev.greenhouseteam.greenhouseconfig.api;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigStorage;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GreenhouseConfigHolder<T> {

    int getConfigVersion();

    String getModId();

    default T get() {
        return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<T>) this);
    }

    T getDefaultServerValue();
    T getDefaultClientValue();

    class Builder<T> {
        private final String modId;
        private int configVersion = 1;
        private T defaultServerValue;
        private T defaultClientValue;
        private Codec<T> serverCodec;
        private Codec<T> clientCodec;
        private StreamCodec<RegistryFriendlyByteBuf, T> networkCodec;
        private BiConsumer<HolderLookup.Provider, T> postRegistryPopulationConsumer;
        private final ImmutableMap.Builder<Integer, Codec<T>> backwardsCompatCodecs = ImmutableMap.builder();

        public Builder(String modId) {
            this.modId = modId;
        }

        public Builder<T> configVersion(int configVersion) {
            this.configVersion = Math.max(1, configVersion);
            return this;
        }

        public Builder<T> commonCodec(Codec<T> codec, T defaultValue) {
            this.serverCodec =  codec;
            this.clientCodec = codec;
            this.defaultServerValue = defaultValue;
            this.defaultClientValue = defaultValue;
            return this;
        }

        public Builder<T> serverCodec(Codec<T> codec, T defaultValue) {
            this.serverCodec =  codec;
            this.defaultServerValue = defaultValue;
            return this;
        }

        public Builder<T> clientCodec(Codec<T> codec, T defaultValue) {
            this.clientCodec =  codec;
            this.defaultClientValue = defaultValue;
            return this;
        }

        public Builder<T> networkCodec(StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {
            this.networkCodec = networkCodec;
            return this;
        }

        public Builder<T> addBackwardsCompatCodec(int version, Codec<T> codec) {
            if (version < this.configVersion)
                throw new IllegalArgumentException("Cannot add backwards compatibility codec for version '" + version + "' for mod '" + modId + "'. Make sure to specify your current config version prior to any backwards compat codecs!");

            backwardsCompatCodecs.put(version, codec);
            return this;
        }

        public Builder<T> postRegistryPopulation(BiConsumer<HolderLookup.Provider, T> consumer) {
            this.postRegistryPopulationConsumer = consumer;
            return this;
        }

        public GreenhouseConfigHolder<T> buildAndRegister() {
            if (modId == null)
                throw new UnsupportedOperationException("Attempted to build config without a modid.");

            if (serverCodec == null && clientCodec == null)
                throw new UnsupportedOperationException("Attempted to build config for mod " + modId + "without any associated codec.");

            if (defaultServerValue == null && defaultClientValue == null)
                throw new UnsupportedOperationException("Attempted to build config without a default value.");

            GreenhouseConfigHolderImpl<T> config = new GreenhouseConfigHolderImpl<>(this.modId, this.configVersion, this.defaultServerValue, this.defaultClientValue, this.serverCodec, this.clientCodec, this.networkCodec, this.postRegistryPopulationConsumer, this.backwardsCompatCodecs.build());

            if (serverCodec != null)
                GreenhouseConfigHolderRegistry.registerServerConfig(this.modId, config);

            if (clientCodec != null)
                GreenhouseConfigHolderRegistry.registerClientConfig(this.modId, config);

            return config;
        }
    }
}
