package com.vaultsniper.mixin;

import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.entity.vault.VaultSharedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VaultBlockEntity.class)
public interface VaultBlockEntityAccessor {

    @Accessor("sharedData")
    VaultSharedData vaultsniper$getSharedData();
}