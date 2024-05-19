package dev.greenhouseteam.greenhouseconfig.client;

import dev.greenhouseteam.greenhouseconfig.impl.client.GreenhouseConfigClient;
import net.fabricmc.api.ClientModInitializer;

public class GreenhouseConfigFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GreenhouseConfigClient.init();
    }
}
