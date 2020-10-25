package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.ArmorHide;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase {
    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void onRenderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci) {
        if (ArmorHide.INSTANCE.isEnabled()) {
            if ((ArmorHide.INSTANCE.getPlayer().getValue()) && entityLivingBaseIn instanceof EntityPlayer) {
                if (ArmorHide.shouldHidePiece(slotIn)) ci.cancel();
            } else if ((ArmorHide.INSTANCE.getArmourStand().getValue()) && entityLivingBaseIn instanceof EntityArmorStand) {
                if (ArmorHide.shouldHidePiece(slotIn)) ci.cancel();
            } else if ((ArmorHide.INSTANCE.getMobs().getValue()) && entityLivingBaseIn instanceof EntityMob) {
                if (ArmorHide.shouldHidePiece(slotIn)) ci.cancel();
            }
        }
    }
}