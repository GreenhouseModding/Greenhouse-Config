package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.SplitConfig;
import dev.greenhouseteam.greenhouseconfig.test.config.TestConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@Mod(GreenhouseConfigTest.MOD_ID)
public class GreenhouseConfigTestNeoForge {
    public GreenhouseConfigTestNeoForge(IEventBus bus) {
        GreenhouseConfigTest.init();
    }

    @EventBusSubscriber(modid = GreenhouseConfigTest.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    private static class GameEvents {
        @SubscribeEvent
        public static void onPostPopulation(GreenhouseConfigEvents.PostPopulation<?> event) {
            if (event.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "_main") && event.getConfig() instanceof TestConfig testConfig) {
                GreenhouseConfigTest.LOG.info(testConfig.redBlocks().toString());
                GreenhouseConfigTest.LOG.info(testConfig.greenBiomes().toString());
            }
            if (event.getConfigName().equals(GreenhouseConfigTest.MOD_ID + "_split") && event.getConfig() instanceof SplitConfig splitConfig) {
                GreenhouseConfigTest.LOG.info(splitConfig.color().serialize());
                if (event.getSide() == ConfigSide.CLIENT)
                    GreenhouseConfigTest.LOG.info(splitConfig.clientValues().color().serialize());
            }
        }
    }
}