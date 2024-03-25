package dev.greenhouseteam.greenhouseconfig.platform;

import dev.greenhouseteam.greenhouseconfig.api.ConfigSide;
import dev.greenhouseteam.greenhouseconfig.impl.GreenhouseConfigFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class GreenhouseConfigFabricPlatformHelper implements GHConfigIPlatformHelper {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ConfigSide getSide() {
        return GreenhouseConfigFabric.isDedicatedServerContext() ? ConfigSide.SERVER : ConfigSide.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
