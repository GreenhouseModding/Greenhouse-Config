package dev.greenhouseteam.greenhouseconfig.platform;

import dev.greenhouseteam.greenhouseconfig.GreenhouseConfigNeoForge;
import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigEvents;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigHolder;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class GreenhouseConfigNeoForgePlatformHelper implements GHConfigIPlatformHelper {
    @Override
    public Platform getPlatform() {
        return Platform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public ConfigSide getSide() {
        return GreenhouseConfigNeoForge.isDedicatedServerContext() ? ConfigSide.SERVER : ConfigSide.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public <T> void postLoadEvent(GreenhouseConfigHolder<T> holder, T config, ConfigSide side) {
        GreenhouseConfigEvents.PostLoad.post(holder, config, side);
    }

    @Override
    public <T> void postPopulationEvent(GreenhouseConfigHolder<T> holder, T config, ConfigSide side) {
        GreenhouseConfigEvents.PostPopulation.post(holder, config, side);
    }
}