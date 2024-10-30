package dev.greenhouseteam.greenhouseconfig.test.config;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.greenhouseconfig.api.codec.GreenhouseConfigStreamCodecs;
import dev.greenhouseteam.greenhouseconfig.api.util.Late;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolder;
import dev.greenhouseteam.greenhouseconfig.impl.util.LateHolderSetImpl;
import dev.greenhouseteam.greenhouseconfig.api.codec.GreenhouseConfigCodecs;
import dev.greenhouseteam.greenhouseconfig.test.GreenhouseConfigTest;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record TestConfig(int silly, Holder<Enchantment> favoriteEnchantment, HolderSet<Block> redBlocks, HolderSet<Biome> greenBiomes) {
    public static final TagKey<Biome> GREENS = TagKey.create(Registries.BIOME, GreenhouseConfigTest.asResource("greens"));
    public static final TestConfig DEFAULT = new TestConfig(69, LateHolder.create(Enchantments.FROST_WALKER), LateHolderSetImpl.createFromEntries(Registries.BLOCK, List.of(ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("netherrack")))), LateHolderSetImpl.createMixed(Registries.BIOME, List.of(GREENS), List.of(Biomes.BAMBOO_JUNGLE)));

    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(Codec.INT, "The value which makes this config very silly."), "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderCodec(Registries.ENCHANTMENT), "Your favorite enchantment.", "Note: Pug was not biased when she set it to hers."), "favorite_enchantment", DEFAULT.favoriteEnchantment()).forGetter(TestConfig::favoriteEnchantment),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BLOCK), "One block, two block, red block, blue block."), "red_blocks", DEFAULT.redBlocks()).forGetter(TestConfig::redBlocks),
            GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.commentedCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "Biomes that are green", "This is an extra line to show how green they really are!"), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
    ).apply(inst, TestConfig::new));

    public static final StreamCodec<FriendlyByteBuf, TestConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            TestConfig::silly,
            GreenhouseConfigStreamCodecs.lateHolderStreamCodec(Registries.ENCHANTMENT),
            TestConfig::favoriteEnchantment,
            GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BLOCK),
            TestConfig::redBlocks,
            GreenhouseConfigStreamCodecs.lateHolderSetStreamCodec(Registries.BIOME),
            TestConfig::greenBiomes,
            TestConfig::new
    );

    public List<? extends Late> getLateValues() {
        ImmutableList.Builder<Late> builder = ImmutableList.builder();
        builder.add(cast(favoriteEnchantment));
        builder.add(cast(redBlocks));
        builder.add(cast(greenBiomes));
        return builder.build();
    }

    private Late cast(Object obj) {
        if (!(obj instanceof Late))
            throw new RuntimeException("The specified object is not late.");
        return (Late)obj;
    }

    public static class CompatCodecs {
        public static final Codec<TestConfig> V1 = RecordCodecBuilder.create(inst -> inst.group(
                GreenhouseConfigCodecs.defaultFieldCodec(Codec.INT, "silly", DEFAULT.silly()).forGetter(TestConfig::silly),
                GreenhouseConfigCodecs.defaultFieldCodec(GreenhouseConfigCodecs.lateHolderSetCodec(Registries.BIOME), "green_biomes", DEFAULT.greenBiomes()).forGetter(TestConfig::greenBiomes)
        ).apply(inst, (t1, t2) -> new TestConfig(t1, DEFAULT.favoriteEnchantment(), DEFAULT.redBlocks(), t2)));
    }
}
