package dev.greenhouseteam.greenhouseconfig.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.impl.codec.LateHolderSetCodec;
import dev.greenhouseteam.greenhouseconfig.impl.codec.CommentedCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GreenhouseConfigCodecs {
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return codec.optionalFieldOf(name).xmap(
                Optional::orElseThrow,
                Optional::of
        );
    }

    public static <A> Codec<A> commentedCodec(List<String> comments, Codec<A> codec) {
        return new CommentedCodec<>(comments, codec);
    }

    public static <E> LateHolderSetCodec<E> lateHoldersCodec(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> lateHoldersStreamCodec(final ResourceKey<? extends Registry<T>> registryKey) {
        return new StreamCodec<>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec = ResourceKey.streamCodec(registryKey);

            public HolderSet<T> decode(RegistryFriendlyByteBuf buf) {
                int i = VarInt.read(buf) - 1;
                if (i == -1) {
                    return LateHolderSet.createFromTags((ResourceKey<Registry<T>>) registryKey, List.of(TagKey.create(registryKey, ResourceLocation.STREAM_CODEC.decode(buf))));
                } else {
                    List<TagKey<T>> tags = new ArrayList<>(Math.min(i, 65536));
                    List<ResourceKey<T>> entries = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; ++j) {
                        boolean isTag = buf.readBoolean();
                        if (isTag)
                            tags.add(TagKey.create(registryKey, ResourceLocation.STREAM_CODEC.decode(buf)));
                        else
                            entries.add(holderCodec.decode(buf));
                    }

                    return LateHolderSet.createMixed((ResourceKey<Registry<T>>) registryKey, tags, entries);
                }
            }

            public void encode(RegistryFriendlyByteBuf buf, HolderSet<T> holderSet) {
                Optional<TagKey<T>> optional = holderSet.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(buf, -1);
                    ResourceLocation.STREAM_CODEC.encode(buf, ((TagKey) optional.get()).location());
                } else {
                    VarInt.write(buf, holderSet.size() + 1);
                    if (holderSet instanceof LateHolderSet<T> late) {

                        for (Either<TagKey<T>, ResourceKey<T>> value : late.keys()) {
                            if (value.left().isPresent()) {
                                buf.writeBoolean(true);
                                ResourceLocation.STREAM_CODEC.encode(buf, value.left().orElseThrow().location());
                            } else {
                                buf.writeBoolean(false);
                                holderCodec.encode(buf, value.right().orElseThrow());
                            }
                        }
                    } else {
                        for (Holder<T> value : holderSet) {
                            buf.writeBoolean(false);
                            holderCodec.encode(buf, value.unwrapKey().orElseThrow());
                        }
                    }
                }

            }
        };
    }
}