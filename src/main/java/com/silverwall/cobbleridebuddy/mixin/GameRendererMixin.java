package com.silverwall.cobbleridebuddy.mixin;

import com.silverwall.cobbleridebuddy.client.GlideCameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void cobbleridebuddy$cinematicGlideFov(Camera camera, float partialTick, boolean changingFov,
                                                  CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() * GlideCameraState.cinematicFovMultiplier());
    }
}
