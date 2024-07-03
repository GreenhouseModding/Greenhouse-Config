# Greenhouse Config
Greenhouse Config is a config library for Fabric and NeoForge made for mostly myself (MerchantPug), however, any developer is free to utilise it and provide feedback for it.

## Features
- A config system based on Mojang's Codec system but adapted to utilise commented config file formats like jsonc.
- A simple config builder that will handle the heavywork for you.
- Config syncing between servers and clients, without changing the client's individual values.
- LateHolderSet, a HolderSet/RegistryEntryList that can be resolved after datapack registries, but can be loaded before then.

## Usage
To learn how to use Greenhouse Config, go to the [Getting Started](https://github.com/GreenhouseTeam/greenhouse-config/wiki/Getting-Started-%E2%80%90-1.x.x) page.

I would suggest checking out the [Wiki](https://github.com/GreenhouseTeam/greenhouse-config/wiki/).

## Maven
Greenhouse Config is on the Greenhouse Team Maven, to get the mod in your environment, please use the bottom as a reference.

```groovy
repositories {
    maven {
        name = "Greenhouse Maven"
        url = "https://maven.greenhouseteam.dev/releases/"
    }
}

dependencies {
    // Depend on the Common project, for VanillaGradle and ModDevGradle.
    compileOnly("dev.greenhouseteam:greenhouseconfig-common-mojmap:${ghc_version}+${minecraft_version}")

    // Depend on the Common project, for Loom.
    modCompileOnly("dev.greenhouseteam:greenhouseconfig-common-intermediary:${ghc_version}+${minecraft_version}")

    // Depend on the Fabric project, for Loom.
    modImplementation(include("dev.greenhouseteam:greenhouseconfig-fabric:${ghc_version}+${minecraft_version}"))
    
    // Depend on the NeoForge project, for NeoGradle.
    jarJar(implementation("dev.greenhouseteam:greenhouseconfig-neoforge:[${ghc_version}+${minecraft_version},)")) {
        version {
            prefer "${ghc_version}+${minecraft_version}"
        }
    }
    
    // Depend on the NeoForge project, for ModDevGradle.
    jarJar("dev.greenhouseteam:greenhouseconfig-neoforge:${ghc_version}+${minecraft_version}")
}
```