package dev.greenhouseteam.greenhouseconfig.impl.client;

import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigStorage;
import net.minecraft.core.HolderLookup;

public class GreenhouseConfigClient {
    public static void init() {
        GreenhouseConfigStorage.generateClientConfigs();
    }

    public static void onWorldJoin(HolderLookup.Provider registries) {
        GreenhouseConfigStorage.onRegistryPopulation(registries);
    }

    public static void onWorldLeave() {
        GreenhouseConfigStorage.onRegistryDepopulation();
    }
}
