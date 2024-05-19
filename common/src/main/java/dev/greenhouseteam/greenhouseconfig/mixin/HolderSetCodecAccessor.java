package dev.greenhouseteam.greenhouseconfig.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.HolderSetCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(HolderSetCodec.class)
public interface HolderSetCodecAccessor<E> {
    @Accessor("homogenousListCodec")
    Codec<List<Holder<E>>> greenhouseconfig$getHomogenousListCodec();
}
