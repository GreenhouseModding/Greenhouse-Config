package dev.greenhouseteam.greenhouseconfig.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import dev.greenhouseteam.greenhouseconfig.api.lang.CommentedValue;

import net.minecraft.resources.DelegatingOps;

import java.util.List;

public class CommentedCodec<T> implements Codec<T> {
    protected final List<String> comments;
    protected final Codec<T> baseCodec;

    public CommentedCodec(List<String> comment, Codec<T> codec) {
        this.comments = comment;
        this.baseCodec = codec;
    }

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        return baseCodec.decode(ops, input);
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        DataResult<T1> result = baseCodec.encode(input, ops, prefix);
        if (result.hasResultOrPartial() && result.getPartialOrThrow() instanceof CommentedValue commented) {
            commented = commented.withComment(comments.toArray(String[]::new));
            return DataResult.success((T1) commented);
        }
        return result;
    }
}
