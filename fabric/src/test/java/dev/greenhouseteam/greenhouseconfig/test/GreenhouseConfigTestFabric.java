package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigSide;
import dev.greenhouseteam.greenhouseconfig.test.config.SplitConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class GreenhouseConfigTestFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GreenhouseConfigTest.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GreenhouseConfigTest.registerServerReloadCommands(dispatcher)
        );
        GreenhouseConfigEvents.POST_POPULATION.register((holder, config, side) -> {
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "/main") && config instanceof TestConfig testConfig) {
                GreenhouseConfigTest.LOG.info("Main Config Values...");
                GreenhouseConfigTest.LOG.info("Silly: {}", testConfig.silly());
                GreenhouseConfigTest.LOG.info(testConfig.redBlocks().toString());
                GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            }
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "/split") && config instanceof SplitConfig splitConfig) {
                GreenhouseConfigTest.LOG.info("Split Config Values...");
                GreenhouseConfigTest.LOG.info(splitConfig.color().formatValue());
                if (side == GreenhouseConfigSide.CLIENT)
                    GreenhouseConfigTest.LOG.info(splitConfig.clientValues().color().formatValue());
            }
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "/server") && config instanceof SplitConfig splitConfig) {
                GreenhouseConfigTest.LOG.info("Server Config Values...");
                GreenhouseConfigTest.LOG.info(splitConfig.color().formatValue());
            }
            if (holder.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "/client") && config instanceof SplitConfig splitConfig && side == GreenhouseConfigSide.CLIENT) {
                GreenhouseConfigTest.LOG.info("Client Config Values...");
                GreenhouseConfigTest.LOG.info(splitConfig.color().formatValue());
                GreenhouseConfigTest.LOG.info(splitConfig.clientValues().color().formatValue());
            }
        });
    }
}