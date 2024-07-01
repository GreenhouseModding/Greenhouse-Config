package dev.greenhouseteam.greenhouseconfig.test.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.greenhouseconfig.impl.codec.LateHolderSetCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record TestConfig(int silly, HolderSet<Block> redBlocks, HolderSet<Biome> greenBiomes) {
    public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));
    public static final TestConfig DEFAULT = new TestConfig(69, LateHolderSet.createFromEntries(Registries.BLOCK, List.of(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))), LateHolderSet.createMixed(Registries.BIOME, List.of(GREENS), List.of(Biomes.BAMBOO_JUNGLE)));

    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("The value which makes this config very silly."), Codec.INT), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("One block, two block, red block, blue block."), GreenhouseConfigCodecs.lateHoldersCodec(Registries.BLOCK)), "red_blocks", DEFAULT.redBlocks()).forGetter(TestConfig::redBlocks),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(List.of("Biomes that are green", "This is an extra line to show how green they really are!"), GreenhouseConfigCodecs.lateHoldersCodec(Registries.BIOME)), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
    ).apply(inst, TestConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TestConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            TestConfig::silly,
            GreenhouseConfigCodecs.lateHoldersStreamCodec(Registries.BLOCK),
            TestConfig::redBlocks,
            GreenhouseConfigCodecs.lateHoldersStreamCodec(Registries.BIOME),
            TestConfig::greenBiomes,
            TestConfig::new
    );

    public static class CompatCodecs {
        public static final Codec<TestConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.lateHoldersCodec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2) -> new TestConfig(t1, DEFAULT.redBlocks(), t2)));
    }
}
