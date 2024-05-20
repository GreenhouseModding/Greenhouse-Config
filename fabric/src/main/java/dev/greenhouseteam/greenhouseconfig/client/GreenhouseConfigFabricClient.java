package dev.greenhouseteam.greenhouseconfig.client;

import dev.greenhouseteam.greenhouseconfig.impl.client.GreenhouseConfigClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class GreenhouseConfigFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GreenhouseConfigClient.init();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            GreenhouseConfigClient.onWorldJoin(client.level.registryAccess());
        });
    }
}
