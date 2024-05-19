package dev.greenhouseteam.greenhouseconfig.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HolderSet.Named.class)
public interface HolderSetNamedAccessor<T> {
    @Invoker("bind")
    void greenhouseconfig$invokeBind(List<Holder<T>> $$0);
}
