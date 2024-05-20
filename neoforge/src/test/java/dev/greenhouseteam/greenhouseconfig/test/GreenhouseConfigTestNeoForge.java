package dev.greenhouseteam.greenhouseconfig.test;

import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(GreenhouseConfig.MOD_ID)
public class GreenhouseConfigTestNeoForge {
    public GreenhouseConfigTestNeoForge(IEventBus bus) {
        GreenhouseConfigTest.init();
    }
}
