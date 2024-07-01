package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.test.config.SplitConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.fabricmc.api.ModInitializer;

public class GreenhouseConfigTestFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GreenhouseConfigTest.init();
        GreenhouseConfigEvents.POST_POPULATION.register((holder, config, side) -> {
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "_main") && config instanceof TestConfig testConfig) {
                GreenhouseConfigTest.LOG.info(testConfig.redBlocks().toString());
                GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            }
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "_split") && config instanceof SplitConfig splitConfig) {
                GreenhouseConfigTest.LOG.info(splitConfig.color().formatValue());
                if (side == ConfigSide.CLIENT)
                    GreenhouseConfigTest.LOG.info(splitConfig.clientValues().color().formatValue());
            }
        });
    }
}