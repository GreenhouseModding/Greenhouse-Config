package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.ConfigHolder;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.test.config.SplitConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfigTest {
    public static final String MOD_ID = "greenhouseconfig_test";
    public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config Test");

    public static final ConfigHolder<TestConfig> CONFIG = new ConfigHolder.Builder<TestConfig>(MOD_ID + "_main")
            /*
            .configVersion(1)
            .commonCodec(TestConfig.CompatCodecs.V1, TestConfig.DEFAULT)
            */
            .schemaVersion(2)
            .common(TestConfig.CODEC, TestConfig.DEFAULT)
            .networkSerializable(TestConfig.STREAM_CODEC)
            .postRegistryPopulation((provider, testConfig) -> {
                LateHolderSet.bind(
                        BuiltInRegistries.BLOCK.asLookup(),
                        testConfig.redBlocks(),
                        s -> LOG.error("Error while parsing \"red_blocks\" in config/greenhouseconfig_test.jsonc: {}", s)
                );
                LateHolderSet.bind(
                        provider.lookupOrThrow(Registries.BIOME),
                        testConfig.greenBiomes(),
                        s -> LOG.error("Error while parsing \"green_biomes\" in config/greenhouseconfig_test.jsonc: {}", s)
                );
            })
            .backwardsCompatCommon(1, TestConfig.CompatCodecs.V1)
            .buildAndRegister();

    public static final ConfigHolder<SplitConfig> SPLIT = new ConfigHolder.Builder<SplitConfig>(MOD_ID + "_split")
            .schemaVersion(1)
            .server(SplitConfig.SERVER_CODEC, SplitConfig.DEFAULT)
            .client(SplitConfig.CLIENT_CODEC, SplitConfig.DEFAULT)
            .networkSerializable(SplitConfig::streamCodec)
            .buildAndRegister();

    public static void init() {
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

}
