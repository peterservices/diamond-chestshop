package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(value = SignBlock.class, priority = 999)
public abstract class SignBlockMixin extends BaseEntityBlock {
    
    protected SignBlockMixin(Properties properties) {
        super(properties);
    }

    //remove shop from chest
    @Override
    public BlockState playerWillDestroy(Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!world.isClientSide() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondchestshop_getShop()) {
            BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
            List<ItemEntity> entities = world.getEntitiesOfClass(ItemEntity.class, new AABB(hangingPos.getX() - 2, hangingPos.getY() - 2, hangingPos.getZ() - 2, hangingPos.getX() + 2, hangingPos.getY() + 2, hangingPos.getZ() + 2));
            while (entities.size() > 0) {
                if (entities.get(0).getUUID().equals(((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_getItemEntity())) {
                    entities.get(0).kill((ServerLevel)world);
                }
                entities.remove(0);
            }
            if (world.getBlockEntity(hangingPos) instanceof BaseContainerBlockEntity shop) {
                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setShop(false);
                DiamondChestShop.getDatabaseManager().removeShop(((BaseContainerBlockEntityInterface)shop).diamondchestshop_getId());
                shop.setChanged();
            }
        }
        return state;
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_useMixin(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());
            Item item = itemStack.getItem();
            SignBlockEntity signEntity = (SignBlockEntity)level.getBlockEntity(blockPos);
            if (signEntity == null) return;
            SignBlockEntityInterface iSign = (SignBlockEntityInterface) signEntity;

            if (iSign.diamondchestshop_getShop()) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    iSign.diamondchestshop_setAdminShop(!iSign.diamondchestshop_getAdminShop());
                    signEntity.setChanged();
                    player.displayClientMessage(Component.literal((iSign.diamondchestshop_getAdminShop()) ? "Created admin shop" : "Removed admin shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }
            }

            //create the chest shop
            if (!item.equals(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencies[0])).get().value())) {
                return;
            }

            if (iSign.diamondchestshop_getShop()) {
                player.displayClientMessage(Component.literal("This is already a shop"), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            BlockPos hangingPos = blockPos.offset(blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), blockState.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
            BlockEntity chestEntity = level.getBlockEntity(hangingPos);
            if (!(chestEntity instanceof RandomizableContainerBlockEntity shop)) {
                player.displayClientMessage(Component.literal("Sign must be on a valid container"), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            BaseContainerBlockEntityInterface iShop = ((BaseContainerBlockEntityInterface) shop);

            if (!iSign.diamondchestshop_getOwner().equals(player.getStringUUID()) || !iShop.diamondchestshop_getOwner().equals(player.getStringUUID())) {
                player.displayClientMessage(Component.literal("You must have placed down the sign and chest"), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            if (player.getOffhandItem().getItem().equals(Items.AIR)) {
                player.displayClientMessage(Component.literal("The sell item must be in your offhand"), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            if (iShop.diamondchestshop_getShop()) {
                player.displayClientMessage(Component.literal("That chest already is a shop"), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            if (!signEntity.getFrontText().getMessage(0,true).getString().toLowerCase().contains("sell") && !signEntity.getFrontText().getMessage(0,true).getString().toLowerCase().contains("buy")) {
                player.displayClientMessage(Component.literal("The first line must be either \"Buy\" or \"Sell\""), true);
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

                int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(signEntity.getFrontText().getMessage(1,true).getString()));
                int money = Integer.parseInt(DiamondChestShop.signTextToReadable(signEntity.getFrontText().getMessage(2,true).getString()));
                if (quantity >= 1) {
                    if (money >= 0) {
                        iSign.diamondchestshop_setShop(true);
                        iShop.diamondchestshop_setShop(true);
                        String itemStr = BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem()).toString();
                        iShop.diamondchestshop_setItem(itemStr);
                        DataComponentMap components = player.getOffhandItem().getComponents();
                        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
                        String tag = DataComponentMap.CODEC.encodeStart(registryOps, components).getOrThrow().toString();
                        iShop.diamondchestshop_setTag(tag);
                        iShop.diamondchestshop_setId(DiamondChestShop.getDatabaseManager().addShop(itemStr, tag));
                        signEntity.setWaxed(true);
                        signEntity.setChanged();
                        shop.setChanged();

                        ItemEntity itemEntity = EntityType.ITEM.create(level, EntitySpawnReason.TRIGGERED);
                        itemEntity.setItem(new ItemStack(player.getOffhandItem().getItem(), Math.min(quantity, player.getOffhandItem().getItem().getDefaultMaxStackSize())));
                        itemEntity.setUnlimitedLifetime();
                        itemEntity.setNeverPickUp();
                        itemEntity.setInvulnerable(true);
                        itemEntity.setNoGravity(true);
                        itemEntity.setPos(new Vec3(hangingPos.getX() + 0.5, hangingPos.getY() + 1.05, hangingPos.getZ() + 0.5));
                        ((ItemEntityInterface) itemEntity).diamondchestshop_setShop(true);
                        level.addFreshEntity(itemEntity);
                        ((SignBlockEntityInterface) signEntity).diamondchestshop_setItemEntity(itemEntity.getUUID());
                        BlockState shopBlock = level.getBlockState(hangingPos);
                        if (shopBlock.getBlock().equals(Blocks.CHEST) && !ChestBlock.getBlockType(shopBlock).equals(DoubleBlockCombiner.BlockType.SINGLE)) {
                            Direction dir = ChestBlock.getConnectedDirection(shopBlock);
                            BlockEntity be2 = level.getBlockEntity(new BlockPos(shop.getBlockPos().getX() + dir.getStepX(), shop.getBlockPos().getY(), shop.getBlockPos().getZ() + dir.getStepZ()));
                            if (be2 != null) {
                                ((BaseContainerBlockEntityInterface) be2).diamondchestshop_setShop(true);
                            }
                        }
                        player.displayClientMessage(Component.literal("Created shop with " + quantity + " " + Component.translatable(player.getOffhandItem().getItem().getDescriptionId()).getString() + (signEntity.getFrontText().getMessage(0,true).getString().toLowerCase().contains("sell") ? (((signEntity.getFrontText().getMessage(0,true).getString().toLowerCase().contains("buy")) ? " sold and bought" : " sold")) : " bought") + " for $" + money), true);
                    } else {
                        player.displayClientMessage(Component.literal("Negative prices are not allowed"), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("Positive quantity required"), true);
                }
                cir.setReturnValue(InteractionResult.PASS);
        }
    }
}