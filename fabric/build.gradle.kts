import house.greenhouse.greenhouseconfig.gradle.Properties
import house.greenhouse.greenhouseconfig.gradle.Versions
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

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

    modCompileOnly("com.terraformersmc:modmenu:${Versions.MOD_MENU}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")
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

tasks.register<RemapJarTask>("remapCommon") {
    dependsOn(project(":common").tasks.jar)
    inputFile.set(project(":common").tasks.jar.get().archiveFile)

    archiveVersion.set("${Versions.MOD}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.register<RemapJarTask>("remapCommonSources") {
    dependsOn(project(":common").tasks.sourcesJar)
    inputFile.set(project(":common").tasks.sourcesJar.get().archiveFile)

    archiveClassifier.set("sources")
    archiveVersion.set("${Versions.MOD}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.register<RemapJarTask>("remapCommonJavadoc") {
    dependsOn(project(":common").tasks.javadocJar)
    inputFile.set(project(":common").tasks.javadocJar.get().archiveFile)

    archiveClassifier.set("javadoc")
    archiveVersion.set("${Versions.MOD}+${Versions.MINECRAFT}-common-intermediary")
}

tasks.getByName("assemble").dependsOn("remapCommon", "remapCommonSources", "remapCommonJavadoc")

publishing {
    publications {
        create<MavenPublication>("mavenIntermediary") {
            artifactId = Properties.MOD_ID
            version = "${Versions.MOD}+${Versions.MINECRAFT}-common-intermediary"
            artifact(tasks["remapCommon"]) {
                builtBy(tasks["remapCommon"])
                classifier = ""
            }
            artifact(tasks["remapCommonSources"]) {
                builtBy(tasks["remapCommonSources"])
                classifier = "sources"
            }
            artifact(tasks["remapCommonJavadoc"]) {
                builtBy(tasks["remapCommonJavadoc"])
                classifier = "javadoc"
            }
        }
    }
}