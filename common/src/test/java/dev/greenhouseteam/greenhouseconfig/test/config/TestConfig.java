package dev.greenhouseteam.greenhouseconfig.test.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.greenhouseconfig.api.GreenhouseConfigCodecs;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record TestConfig(int silly, HolderSet<Biome> greenBiomes) {
    public static final TestConfig DEFAULT = new TestConfig(69, HolderSet.emptyNamed(VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME), TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("green_biomes"))));

    public static final Codec<TestConfig> TEST_CONFIG_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(Biome.LIST_CODEC, "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
    ).apply(inst, TestConfig::new));
}
