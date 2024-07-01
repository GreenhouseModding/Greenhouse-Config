package dev.greenhouseteam.greenhouseconfig.impl;

import dev.greenhouseteam.greenhouseconfig.platform.GreenhouseConfigFabricPlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class GreenhouseConfigFabric implements ModInitializer {
    private static boolean dedicatedServerContext = false;

    @Override
    public void onInitialize() {
        GreenhouseConfig.init(new GreenhouseConfigFabricPlatformHelper());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server.isDedicatedServer()) {
                dedicatedServerContext = true;
                GreenhouseConfig.onServerStarting(server);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(GreenhouseConfig::onServerStarted);
    }

    public static boolean isDedicatedServerContext() {
        return dedicatedServerContext;
    }
}
