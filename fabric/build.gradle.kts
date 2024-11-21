import dev.greenhouseteam.greenhouseconfig.gradle.Properties
import dev.greenhouseteam.greenhouseconfig.gradle.Versions

plugins {
    id("greenhouseconfig.loader")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.INTERNAL_MINECRAFT}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${Versions.INTERNAL_MINECRAFT}:${Versions.PARCHMENT}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

    modImplementation("com.kneelawk.common-events:common-events-fabric:${Versions.COMMON_EVENTS}+${Versions.MINECRAFT}")?.let { include(it) }
}

loom {
    val aw = file("src/main/resources/${Properties.MOD_ID}.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        defaultRefmapName.set("${Properties.MOD_ID}.refmap.json")
    }
    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
        }
        register(Properties.MOD_ID + "_test") {
            sourceSet(sourceSets["test"])
        }
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            runDir("runs/client")
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            programArgs("--username=Dev")
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            runDir("runs/server")
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
    }
}

publishMods {
    github {
        file.set(tasks.named<Jar>("remapJar").get().archiveFile)
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        parent(project(":common").tasks.named("publishGithub"))
    }
}