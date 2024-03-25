package dev.greenhouseteam.greenhouseconfig;

import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigHolderImpl;
import dev.greenhouseteam.greenhouseconfig.platform.GreenhouseConfigNeoForgePlatformHelper;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(GreenhouseConfig.MOD_ID)
public class GreenhouseConfigNeoForge {
    private static boolean dedicatedServerContext = false;

    public GreenhouseConfigNeoForge(IEventBus eventBus) {
        GreenhouseConfig.init(new GreenhouseConfigNeoForgePlatformHelper());
    }

    public static boolean isDedicatedServerContext() {
        return dedicatedServerContext;
    }

    @Mod.EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerAboutToStartEvent event) {
            if (event.getServer().isDedicatedServer()) {
                dedicatedServerContext = true;
            }
        }
    }
}