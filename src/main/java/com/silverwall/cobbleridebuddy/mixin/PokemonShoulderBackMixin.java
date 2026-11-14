package com.silverwall.cobbleridebuddy.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PokemonOnShoulderRenderer.class)
public abstract class PokemonShoulderBackMixin {
    @Redirect(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFFFZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 1
        )
    )
    private void cobbleridebuddy$centerCrobatOnBack(
        PoseStack matrices,
        double x,
        double y,
        double z,
        PoseStack matrixStack,
        MultiBufferSource buffer,
        int packedLight,
        Player livingEntity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        boolean leftShoulder
    ) {
        CompoundTag shoulderData = leftShoulder
            ? livingEntity.getShoulderEntityLeft()
            : livingEntity.getShoulderEntityRight();

        if ("cobblemon:crobat".equals(shoulderData.getString("shoulder_species"))) {
            // The shoulder renderer already moves crouching Pokémon 0.15 blocks
            // forward. Compensate so Crobat stays centered on the back.
            matrices.translate(0.0, y, livingEntity.isCrouching() ? -0.08 : -0.23);
        } else {
            matrices.translate(x, y, z);
        }
    }
}
