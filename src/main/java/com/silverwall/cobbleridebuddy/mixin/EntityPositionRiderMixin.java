package com.silverwall.cobbleridebuddy.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityPositionRiderMixin {
    @Inject(method = "positionRider", at = @At("TAIL"))
    private void cobbleridebuddy$positionCrobatOnBack(Entity passenger, Entity.MoveFunction positionUpdater, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !(passenger instanceof PokemonEntity pokemon)) {
            return;
        }
        if (!"cobblemon:crobat".equals(pokemon.getPokemon().getSpecies().getResourceIdentifier().toString())) {
            return;
        }

        // Yaw 0 faces +Z in Minecraft, so -Z is behind the player.
        double yaw = Math.toRadians(player.getYRot());
        double backOffset = 0.48;
        double x = player.getX() + Math.sin(yaw) * backOffset;
        double z = player.getZ() - Math.cos(yaw) * backOffset;
        double y = player.getY() + 0.58;

        positionUpdater.accept(passenger, x, y, z);
        if (passenger instanceof LivingEntity living) {
            living.setYRot(player.getYRot());
            living.setYBodyRot(player.getYRot());
            living.setYHeadRot(player.getYHeadRot());
        }
    }
}
