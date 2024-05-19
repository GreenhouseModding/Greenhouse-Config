package dev.greenhouseteam.greenhouseconfig.test.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.greenhouseconfig.api.codec.LateHolderSetCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.List;

public record TestConfig(int silly, HolderSet<Biome> greenBiomes) {
    public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));
    public static final TestConfig DEFAULT = new TestConfig(69, new LateHolderSet.Mixed(Registries.BIOME, List.of(GREENS), List.of(Biomes.BAMBOO_JUNGLE)));

    public static final Codec<TestConfig> TEST_CONFIG_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.jsonCCodec(List.of("The value which makes this config very silly."), Codec.INT), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.jsonCCodec(List.of("Biomes that are green", "This is an extra line to show how green they really are!"), LateHolderSetCodec.create(Registries.BIOME)), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
    ).apply(inst, TestConfig::new));
}
