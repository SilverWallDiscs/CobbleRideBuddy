package com.silverwall.cobbleridebuddy.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonOnShoulderRenderer.class)
public abstract class PokemonShoulderBackMixin {
    @Unique
    private static final ThreadLocal<Boolean> cobbleridebuddy$renderingCrobat = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<Boolean> cobbleridebuddy$renderingCrouched = ThreadLocal.withInitial(() -> false);

    private static final String CROBAT_RENDER_METHOD =
        "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFFFZ)V";

    @Inject(method = CROBAT_RENDER_METHOD, at = @At("HEAD"))
    private void cobbleridebuddy$captureShoulderPokemon(
        PoseStack matrices,
        MultiBufferSource buffer,
        int packedLight,
        Player player,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        boolean leftShoulder,
        CallbackInfo ci
    ) {
        CompoundTag shoulderData = leftShoulder
            ? player.getShoulderEntityLeft()
            : player.getShoulderEntityRight();
        cobbleridebuddy$renderingCrobat.set(
            "cobblemon:crobat".equals(shoulderData.getString("shoulder_species"))
        );
        cobbleridebuddy$renderingCrouched.set(player.isCrouching());
    }

    @Redirect(
        method = CROBAT_RENDER_METHOD,
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
            ordinal = 1
        )
    )
    private void cobbleridebuddy$centerCrobatOnBack(PoseStack matrices, double x, double y, double z) {
        if (cobbleridebuddy$renderingCrobat.get()) {
            // The shoulder renderer already translates crouching Pokémon 0.15
            // blocks forward, so compensate to keep Crobat centered on the back.
            matrices.translate(0.0, y, cobbleridebuddy$renderingCrouched.get() ? -0.08 : -0.23);
        } else {
            matrices.translate(x, y, z);
        }
    }

    @Inject(method = CROBAT_RENDER_METHOD, at = @At("RETURN"))
    private void cobbleridebuddy$clearShoulderContext(
        PoseStack matrices,
        MultiBufferSource buffer,
        int packedLight,
        Player player,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        boolean leftShoulder,
        CallbackInfo ci
    ) {
        cobbleridebuddy$renderingCrobat.remove();
        cobbleridebuddy$renderingCrouched.remove();
    }
}
