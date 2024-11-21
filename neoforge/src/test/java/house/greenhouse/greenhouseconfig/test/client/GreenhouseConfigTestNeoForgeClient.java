package house.greenhouse.greenhouseconfig.test.client;

import house.greenhouse.greenhouseconfig.test.GreenhouseConfigTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class GreenhouseConfigTestNeoForgeClient {
    @EventBusSubscriber(modid = GreenhouseConfigTest.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            GreenhouseConfigTestClient.registerClientCommands(event.getDispatcher());
        }
    }
}