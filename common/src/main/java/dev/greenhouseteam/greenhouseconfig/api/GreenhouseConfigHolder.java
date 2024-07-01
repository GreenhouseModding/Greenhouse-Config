package dev.greenhouseteam.greenhouseconfig.api;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigStorage;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderRegistry;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface GreenhouseConfigHolder<T> {

    /**
     * Gets the schema version for this config.
     * @return The schema version.
     */
    int getSchemaVersion();

    /**
     * Gets the config name.
     * @return The config name.
     */
    String getConfigName();

    /**
     * Gets the config of this holder.
     * @return  Returns the config.
     */
    default T get() {
        return GreenhouseConfigStorage.getConfig((GreenhouseConfigHolderImpl<T>) this);
    }

    /**
     * Gets the default value for this config holder.
     * @return  The default value of this config holder.
     */
    T getDefaultValue();

    class Builder<T> {
        private final String configName;
        private int schemaVersion = 1;
        private T defaultServerValue;
        private T defaultClientValue;
        private Codec<T> serverCodec;
        private Codec<T> clientCodec;
        private Function<T, StreamCodec<FriendlyByteBuf, T>> networkCodecFunction;
        private BiConsumer<HolderLookup.Provider, T> postRegistryPopulationCallback;
        private final ImmutableMap.Builder<Integer, Codec<T>> backwardsCompatCodecsServer = ImmutableMap.builder();
        private final Set<Integer> backwardsCompatClientVersions = new HashSet<>();
        private final ImmutableMap.Builder<Integer, Codec<T>> backwardsCompatCodecsClient = ImmutableMap.builder();

        /**
         * Constructs a {@link GreenhouseConfigHolder.Builder} with the specified config name.
         * Configs will be inside the config folder as 'config_name'.jsonc
         *
         * @param configName The name to create a config for.
         */
        public Builder(String configName) {
            this.configName = configName;
        }

        /**
         * Sets the config version.
         * The config's version is accessed through the user defined file metadata
         * 'GreenhouseConfigSchemaVersion'.
         *
         * @param schemaVersion The version of the schema.
         */
        public Builder<T> schemaVersion(int schemaVersion) {
            this.schemaVersion = Math.max(1, schemaVersion);
            return this;
        }

        /**
         * Sets a codec for both the dedicated server and the client/integrated server.
         *
         * @param codec         The codec to use for both environments.
         * @param defaultValue  The default value for this config.
         */
        public Builder<T> common(Codec<T> codec, T defaultValue) {
            server(codec, defaultValue);
            client(codec, defaultValue);
            return this;
        }

        /**
         * The config for use with dedicated servers.
         * <p>
         * This will set the client config is there is no client config value,
         * this is so clients running this mod can still access this config.
         *
         * @param codec         The codec to use for serialization.
         * @param defaultValue  The default server config value.
         */
        public Builder<T> server(Codec<T> codec, T defaultValue) {
            serverCodec =  codec;
            defaultServerValue = defaultValue;
            if (clientCodec == null && defaultClientValue == null)
                client(codec, defaultValue);
            return this;
        }

        /**
         * Sets the config for use with clients and integrated servers.
         *
         * @param codec         The codec to use for serialization.
         * @param defaultValue  The default client config value.
         */
        public Builder<T> client(Codec<T> codec, T defaultValue) {
            clientCodec =  codec;
            defaultClientValue = defaultValue;
            return this;
        }

        /**
         * Sets the config to serialize over the network.
         * Setting specified values from the server on the client.
         * <p>
         * If the client does not have the mod that this config originates
         * from this will be ignored.
         *
         * @param streamCodec   The stream codec to use for serialization.
         */
        public Builder<T> networkSerializable(StreamCodec<FriendlyByteBuf, T> streamCodec) {
            return networkSerializable(clientConfig -> streamCodec);
        }

        /**
         * Sets the config to serialize over the network.
         * Setting specified values from the server on the client.
         *
         * @param streamCodecFunction   The stream codec to use for serialization,
         *                              whilst passing the current client config.
         */
        public Builder<T> networkSerializable(Function<T, StreamCodec<FriendlyByteBuf, T>> streamCodecFunction) {
            networkCodecFunction = streamCodecFunction;
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * an older version of this config to the current version for
         * both the dedicated server and client/integrated server.
         *
         * @param version   The version to convert from.
         * @param codec     The codec used to convert from the old version to the current version.
         */
        public Builder<T> backwardsCompatCommon(int version, Codec<T> codec) {
            backwardsCompatServer(version, codec);
            backwardsCompatClient(version, codec);
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * an older version of this config to the current version for
         * the dedicated server.
         * <p>
         * This will additionally set the client backwards compatibility config is there is no client config value,
         * this is so integrated servers running the mod can still access this config.
         *
         * @param version   The version to convert from.
         * @param codec     The codec used to convert from the old version to the current version.
         */
        public Builder<T> backwardsCompatServer(int version, Codec<T> codec) {
            if (version >= schemaVersion)
                throw new IllegalArgumentException("Cannot add backwards compatibility codec for version '" + version + "' for mod '" + configName + "' as it is equal or larger than the current schema version. Make sure to specify your current config version prior to any backwards compat codecs!");
            backwardsCompatCodecsServer.put(version, codec);
            if (!backwardsCompatClientVersions.contains(version))
                backwardsCompatClient(version, codec);
            return this;
        }

        /**
         * Adds a backwards compatibility codec used for converting from
         * an older version of this config to the current version for
         * the client/integrated server.
         *
         * @param version   The version to convert from.
         * @param codec     The codec used to convert from the old version to the current version.
         */
        public Builder<T> backwardsCompatClient(int version, Codec<T> codec) {
            if (version >= schemaVersion)
                throw new IllegalArgumentException("Cannot add backwards compatibility codec for version '" + version + "' for mod '" + configName + "' as it is equal or larger than the current schema version. Make sure to specify your current config version prior to any backwards compat codecs!");
            backwardsCompatCodecsClient.put(version, codec);
            backwardsCompatClientVersions.add(version);
            return this;
        }

        /**
         * A callback that runs after registries have been populated.
         * Mostly used for binding values
         *
         * @param callback A callback that runs on registry values and the config.
         * @see dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet
         */
        public Builder<T> postRegistryPopulation(BiConsumer<HolderLookup.Provider, T> callback) {
            postRegistryPopulationCallback = callback;
            return this;
        }

        /**
         * Builds and registers this config.
         * @return  A {@link GreenhouseConfigHolder} that holds this config.
         */
        public GreenhouseConfigHolder<T> buildAndRegister() {
            if (configName == null)
                throw new UnsupportedOperationException("Attempted to build config without a modid.");

            if (serverCodec == null && clientCodec == null)
                throw new UnsupportedOperationException("Attempted to build config for mod " + configName + "without any associated codec.");

            if (defaultServerValue == null && defaultClientValue == null)
                throw new UnsupportedOperationException("Attempted to build config without a default value.");

            GreenhouseConfigHolderImpl<T> config = new GreenhouseConfigHolderImpl<>(configName, schemaVersion, defaultServerValue, defaultClientValue, serverCodec, clientCodec, networkCodecFunction, postRegistryPopulationCallback, backwardsCompatCodecsServer.buildKeepingLast(), backwardsCompatCodecsClient.buildKeepingLast());

            if (serverCodec != null)
                GreenhouseConfigHolderRegistry.registerServerConfig(configName, config);

            if (clientCodec != null)
                GreenhouseConfigHolderRegistry.registerClientConfig(configName, config);

            return config;
        }
    }
}
