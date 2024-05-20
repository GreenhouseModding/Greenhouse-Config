package dev.greenhouseteam.greenhouseconfig.test.client;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.fabricmc.api.ClientModInitializer;

public class GreenhouseConfigTestFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GreenhouseConfigEvents.POST_POPULATION.register((holder, config, side) -> {
            if (holder.getModId().equals(GreenhouseConfigTest.MOD_ID) && config instanceof TestConfig testConfig) {
                GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            }
        });
    }
}
