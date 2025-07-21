package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignBlockEntityInterface {
    private String diamondchestshop_owner;
    private UUID diamondchestshop_itemEntity;
    private boolean diamondchestshop_isShop;
    private boolean diamondchestshop_isAdminShop;

    public void diamondchestshop_setOwner(String newOwner) {
        this.diamondchestshop_owner = newOwner;
    }

    public void diamondchestshop_setItemEntity(UUID newEntity) {
        this.diamondchestshop_itemEntity = newEntity;
    }

    public void diamondchestshop_setShop(boolean newShop) {
        this.diamondchestshop_isShop = newShop;
    }

    public void diamondchestshop_setAdminShop(boolean newAdminShop) {
        this.diamondchestshop_isAdminShop = newAdminShop;
    }

    public boolean diamondchestshop_getAdminShop() {
        return this.diamondchestshop_isAdminShop;
    }

    public boolean diamondchestshop_getShop() {
        return this.diamondchestshop_isShop;
    }

    public String diamondchestshop_getOwner() {
        return this.diamondchestshop_owner;
    }

    public UUID diamondchestshop_getItemEntity() {
        return this.diamondchestshop_itemEntity;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void diamondchestshop_saveAdditionalMixin(ValueOutput valueOutput, CallbackInfo ci) {
        if (this.diamondchestshop_isShop) valueOutput.putString("diamondchestshop_ItemEntity", this.diamondchestshop_itemEntity.toString());
        valueOutput.putString("diamondchestshop_ShopOwner", (this.diamondchestshop_owner == null) ? "" : this.diamondchestshop_owner);
        valueOutput.putBoolean("diamondchestshop_IsShop", this.diamondchestshop_isShop);
        valueOutput.putBoolean("diamondchestshop_IsAdminShop", this.diamondchestshop_isAdminShop);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void diamondchestshop_loadMixin(ValueInput valueInput, CallbackInfo ci) {
        this.diamondchestshop_owner = valueInput.getString("diamondchestshop_ShopOwner").orElse(null);
        this.diamondchestshop_isShop = valueInput.getBooleanOr("diamondchestshop_IsShop", false);
        this.diamondchestshop_isAdminShop = valueInput.getBooleanOr("diamondchestshop_IsAdminShop", false);
        if (this.diamondchestshop_isShop) this.diamondchestshop_itemEntity = UUID.fromString(valueInput.getString("diamondchestshop_ItemEntity").get());
    }
}