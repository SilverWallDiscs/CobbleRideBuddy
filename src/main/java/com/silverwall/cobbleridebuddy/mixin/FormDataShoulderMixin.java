package com.silverwall.cobbleridebuddy.mixin;

import com.cobblemon.mod.common.pokemon.FormData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FormData.class, remap = false)
public abstract class FormDataShoulderMixin {
    @Inject(method = "getShoulderMountable", at = @At("RETURN"), cancellable = true, remap = false)
    private void cobbleridebuddy$allowCrobatShoulderMount(CallbackInfoReturnable<Boolean> cir) {
        FormData form = (FormData) (Object) this;
        if ("cobblemon:crobat".equals(form.getSpecies().getResourceIdentifier().toString())) {
            cir.setReturnValue(true);
        }
    }
}
