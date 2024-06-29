package dev.greenhouseteam.greenhouseconfig.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.greenhouseconfig.api.util.LateHolderSet;
import dev.greenhouseteam.greenhouseconfig.impl.codec.LateHolderSetCodec;
import dev.greenhouseteam.greenhouseconfig.impl.jsonc.JsonCCodec;
import io.netty.buffer.ByteBuf;
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
                o -> o.orElse(defaultValue),
                Optional::of
        );
    }

    public static <A> Codec<A> jsonCCodec(List<String> comments, Codec<A> codec) {
        return new JsonCCodec<>(comments, codec);
    }

    public static <E> LateHolderSetCodec<E> lateHoldersCodec(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> lateHoldersStreamCodec(final ResourceKey<? extends Registry<T>> registryKey) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec = ResourceKey.streamCodec(registryKey);

            public HolderSet<T> decode(RegistryFriendlyByteBuf buf) {
                int i = VarInt.read(buf) - 1;
                if (i == -1) {
                    Registry<T> registry = buf.registryAccess().registryOrThrow(registryKey);
                    return LateHolderSet.createFromTags((ResourceKey<Registry<T>>) registryKey, List.of(TagKey.create(registryKey, (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf))));
                } else {
                    var tags = new ArrayList(Math.min(i, 65536));
                    var entries = new ArrayList(Math.min(i, 65536));

                    for (int j = 0; j < i; ++j) {
                        boolean isTag = buf.readBoolean();
                        if (isTag)
                            tags.add(TagKey.create(registryKey, (ResourceLocation) ResourceLocation.STREAM_CODEC.decode(buf)));
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
                        var it = late.keys().iterator();

                        while (it.hasNext()) {
                            var value = it.next();
                            if (value.left().isPresent()) {
                                buf.writeBoolean(true);
                                ResourceLocation.STREAM_CODEC.encode(buf, value.left().orElseThrow().location());
                            } else {
                                buf.writeBoolean(false);
                                holderCodec.encode(buf, value.right().orElseThrow());
                            }
                        }
                    } else {
                        var it = holderSet.iterator();

                        while (it.hasNext()) {
                            var value = it.next();
                            buf.writeBoolean(false);
                            holderCodec.encode(buf, value.unwrapKey().orElseThrow());
                        }
                    }
                }

            }
        };
    }
}