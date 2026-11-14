package com.silverwall.cobbleridebuddy.skill

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.party
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Uses Cobblemon's native ShoulderedState and player shoulder NBT. No passenger
 * entity is left attached to the player.
 */
object RideBuddySkill : CompanionSkill {
    const val CROBAT_ID = "cobblemon:crobat"
    override val id = ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "ride_buddy")

    override fun activate(player: ServerPlayer, target: PokemonEntity?): Boolean {
        target ?: return false
        if (target.pokemon.species.resourceIdentifier.toString() != CROBAT_ID ||
            target.ownerUUID != player.uuid ||
            !target.isAlive ||
            target.isBattling ||
            target.distanceToSqr(player) > 64.0 ||
            !player.onGround() ||
            player.isInWater ||
            player.isPassenger ||
            !target.canSitOnShoulder()
        ) {
            return false
        }

        if (findAttachedPokemon(player) != null) {
            return false
        }

        return target.tryMountingShoulder(player)
    }

    override fun deactivate(player: ServerPlayer): Boolean {
        val pokemon = findAttachedPokemon(player) ?: return false
        pokemon.recall()
        return true
    }

    fun findAttachedPokemon(player: ServerPlayer): Pokemon? {
        return player.party().firstOrNull { pokemon ->
            if (pokemon.species.resourceIdentifier.toString() != CROBAT_ID) {
                return@firstOrNull false
            }

            val state = pokemon.state as? ShoulderedState ?: return@firstOrNull false
            state.playerUUID == player.uuid &&
                state.pokemonUUID == pokemon.uuid &&
                state.isStillShouldered(player)
        }
    }
}
