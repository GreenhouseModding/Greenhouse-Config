package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class GreenhouseConfigTestFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GreenhouseConfigTest.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GreenhouseConfigTest.registerServerReloadCommands(dispatcher)
        );
        GreenhouseConfigEvents.POST_POPULATION.register(GreenhouseConfigTest::logTestConfigs);
    }
}