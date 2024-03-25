package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigStorage;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.minecraft.resources.ResourceLocation;

public class GreenhouseConfigTest {
    private static final String MOD_ID = "greenhouseconfig_test";

    private static final GreenhouseConfigHolder<TestConfig> CONFIG = new GreenhouseConfigHolder.Builder<TestConfig>(MOD_ID)
            .configVersion(1)
            .commonCodec(TestConfig.TEST_CONFIG_CODEC, TestConfig.DEFAULT)
            .buildAndRegister();

    public static void init() {
        GreenhouseConfigStorage.createServerConfigIfMissing((GreenhouseConfigHolderImpl<TestConfig>) CONFIG);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
