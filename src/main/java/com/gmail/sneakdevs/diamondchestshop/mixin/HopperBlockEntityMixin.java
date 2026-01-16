package com.gmail.sneakdevs.diamondchestshop.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;

@Mixin(value = HopperBlockEntity.class, priority = 100)
public class HopperBlockEntityMixin {
    @Inject(method = "canPlaceItemInContainer", at = @At("HEAD"), cancellable = true)
    private static void diamondchestshop_addItemMixin(Container container, ItemStack itemStack, int i, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (DiamondChestShopConfig.getInstance().shopProtectHopper && container instanceof BaseContainerBlockEntity && ((BaseContainerBlockEntityInterface)container).diamondchestshop_getShop()) {
            cir.setReturnValue(false);
        } else if (DiamondChestShopConfig.getInstance().shopProtectHopper && container instanceof CompoundContainer) {
            CompoundContainerAccessor accessor = (CompoundContainerAccessor)container;
            if (((BaseContainerBlockEntityInterface)accessor.getContainer1()).diamondchestshop_getShop() || ((BaseContainerBlockEntityInterface)accessor.getContainer2()).diamondchestshop_getShop()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canTakeItemFromContainer", at = @At("HEAD"), cancellable = true)
    private static void diamondchestshop_addItemMixin(Container container, Container container2, ItemStack itemStack, int i, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (DiamondChestShopConfig.getInstance().shopProtectHopper && container2 instanceof BaseContainerBlockEntity && ((BaseContainerBlockEntityInterface)container2).diamondchestshop_getShop()) {
            cir.setReturnValue(false);
        } else if (DiamondChestShopConfig.getInstance().shopProtectHopper && container2 instanceof CompoundContainer) {
            CompoundContainerAccessor accessor = (CompoundContainerAccessor)container2;
            if (((BaseContainerBlockEntityInterface)accessor.getContainer1()).diamondchestshop_getShop() || ((BaseContainerBlockEntityInterface)accessor.getContainer2()).diamondchestshop_getShop()) {
                cir.setReturnValue(false);
            }
        }
    }
}