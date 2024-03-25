package dev.greenhouseteam.greenhouseconfig.impl;

import dev.greenhouseteam.greenhouseconfig.platform.GHConfigIPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenhouseConfig {


    public static final String MOD_ID = "greenhouseconfig";
    public static final Logger LOG = LoggerFactory.getLogger("Greenhouse Config");
    private static GHConfigIPlatformHelper PLATFORM;

    public static void init(GHConfigIPlatformHelper platform) {
        PLATFORM = platform;
    }

    public static GHConfigIPlatformHelper getPlatform() {
        return PLATFORM;
    }
}