package dev.greenhouseteam.greenhouseconfig.impl;

import dev.greenhouseteam.greenhouseconfig.platform.GreenhouseConfigNeoForgePlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(GreenhouseConfig.MOD_ID)
public class GreenhouseConfigNeoForge {
    private static boolean dedicatedServerContext = false;

    public GreenhouseConfigNeoForge(IEventBus eventBus) {
        GreenhouseConfig.init(new GreenhouseConfigNeoForgePlatformHelper());
    }

    public static boolean isDedicatedServerContext() {
        return dedicatedServerContext;
    }

    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerAboutToStartEvent event) {
            if (event.getServer().isDedicatedServer()) {
                dedicatedServerContext = true;
                GreenhouseConfig.onServerStarting(event.getServer());
            }
        }
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            if (event.getServer().isDedicatedServer()) {
                dedicatedServerContext = true;
                GreenhouseConfig.onServerStarted(event.getServer());
            }
        }
    }
}