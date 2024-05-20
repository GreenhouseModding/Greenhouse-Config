package dev.greenhouseteam.greenhouseconfig.test.client;

import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

public class GreenhouseConfigTestNeoForgeClient {
    @EventBusSubscriber(modid = GreenhouseConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    private static class GameEvents {
        @SubscribeEvent
        public static void onPlayerJoin(GreenhouseConfigEvents.PostPopulation<?> event) {
            if (event.getModId().equals(GreenhouseConfigTest.MOD_ID) && event.getConfig() instanceof TestConfig testConfig) {
                GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            }
        }
    }
}
