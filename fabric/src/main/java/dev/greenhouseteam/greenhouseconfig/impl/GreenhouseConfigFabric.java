package dev.greenhouseteam.greenhouseconfig.impl;

import dev.greenhouseteam.greenhouseconfig.impl.network.SyncGreenhouseConfigPacket;
import dev.greenhouseteam.greenhouseconfig.platform.GreenhouseConfigFabricPlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

public class GreenhouseConfigFabric implements ModInitializer {
    private static boolean dedicatedServerContext = false;

    @Override
    public void onInitialize() {
        GreenhouseConfig.init(new GreenhouseConfigFabricPlatformHelper());

        PayloadTypeRegistry.configurationS2C().register(SyncGreenhouseConfigPacket.TYPE, SyncGreenhouseConfigPacket.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server.isDedicatedServer()) {
                dedicatedServerContext = true;
                GreenhouseConfig.onServerStarting();
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(GreenhouseConfig::onServerStarted);
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register((handler, server) -> {
            if (!ServerConfigurationNetworking.canSend(handler, SyncGreenhouseConfigPacket.TYPE) || !server.isDedicatedServer())
                return;
            GreenhouseConfigStorage.createSyncPackets().forEach(packet -> ServerConfigurationNetworking.send(handler, packet));
        });
    }

    public static boolean isDedicatedServerContext() {
        return dedicatedServerContext;
    }
}
