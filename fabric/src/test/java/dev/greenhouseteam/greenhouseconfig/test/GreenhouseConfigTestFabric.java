package dev.greenhouseteam.greenhouseconfig.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class GreenhouseConfigTestFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GreenhouseConfigTest.init();
    }
}
