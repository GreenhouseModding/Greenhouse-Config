package dev.greenhouseteam.greenhouseconfig.impl.jsonc;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.greenhouseteam.greenhouseconfig.api.CommentedJson;
import dev.greenhouseteam.greenhouseconfig.mixin.DelegatingOpsAccessor;
import net.minecraft.resources.DelegatingOps;

import java.util.List;

public class JsonCCodec<T> implements Codec<T> {
    protected final List<String> comments;
    protected final Codec<T> baseCodec;

    public JsonCCodec(List<String> comment, Codec<T> codec) {
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
        if ((ops instanceof JsonCOps || ops instanceof DelegatingOps<T1> delegatingOps && ((DelegatingOpsAccessor)delegatingOps).greenhouseconfig$getDelegate() instanceof JsonCOps) && result.error().isEmpty()) {
            CommentedJson element = (CommentedJson) result.getOrThrow();
            element = new CommentedJson(element.json(), comments.toArray(String[]::new));
            return DataResult.success((T1)element);
        }
        return result;
    }
}
