package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private static void diamondchestshop_updateCustomBlockEntityTagMixin(Level world, Player player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (world.getServer() != null && player != null) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof BaseContainerBlockEntity) {
                BlockState bs = be.getBlockState();
                ((BaseContainerBlockEntityInterface) be).diamondchestshop_setOwner(player.getStringUUID());
                if (bs.getBlock().equals(Blocks.CHEST) && !ChestBlock.getBlockType(bs).equals(DoubleBlockCombiner.BlockType.SINGLE)) {
                    Direction dir = ChestBlock.getConnectedDirection(bs);
                    BlockEntity be2 = world.getBlockEntity(new BlockPos(be.getBlockPos().getX() + dir.getStepX(), be.getBlockPos().getY(), be.getBlockPos().getZ() + dir.getStepZ()));
                    if (be2 != null && ((BaseContainerBlockEntityInterface) be2).diamondchestshop_getShop()) {
                        ((BaseContainerBlockEntityInterface) be).diamondchestshop_setShop(true);
                    };
                }
            } else if (be instanceof SignBlockEntity) {
                ((SignBlockEntityInterface) be).diamondchestshop_setOwner(player.getStringUUID());
            }
        }
    }
}