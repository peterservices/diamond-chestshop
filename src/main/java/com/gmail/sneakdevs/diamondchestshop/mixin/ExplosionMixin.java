package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ServerExplosion.class)
public class ExplosionMixin {

    @Shadow @Final
    private ServerLevel level;

    @ModifyVariable(method = "interactWithBlocks", at = @At("HEAD"), argsOnly = true)
    private List<BlockPos> diamondchestshop_filterExplodedBlocks(List<BlockPos> value) {
        if (!DiamondChestShopConfig.getInstance().shopProtectExplosion) {
            return value;
        }

        // Remove protected blocks
        value.removeIf((b) -> {
            BlockEntity be = this.level.getBlockEntity(b);
            return be != null && ((be instanceof SignBlockEntity && ((SignBlockEntityInterface) be).diamondchestshop_getShop()) || (be instanceof BaseContainerBlockEntity && ((BaseContainerBlockEntityInterface) be).diamondchestshop_getShop()));
        });

        return value;
    }
}