package com.silverwall.cobbleridebuddy.service

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.silverwall.cobbleridebuddy.network.BuddyActionPayload
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object BuddyService {
    const val ATTACHED_TAG = "cobbleridebuddy.back_buddy"
    private const val CROBAT_ID = "cobblemon:crobat"
    private const val MAX_INTERACTION_DISTANCE_SQUARED = 64.0

    fun handleAction(player: ServerPlayer, payload: BuddyActionPayload) {
        val requestedPokemon = payload.pokemonUuid
        if (requestedPokemon == null) {
            release(player)
            return
        }

        val currentBuddy = findBuddy(player)
        if (currentBuddy != null) {
            if (currentBuddy.uuid == requestedPokemon) release(player)
            else player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.already_attached"))
            return
        }

        val pokemon = player.serverLevel().getEntity(requestedPokemon) as? PokemonEntity
        if (pokemon == null ||
            pokemon.pokemon.species.resourceIdentifier.toString() != CROBAT_ID ||
            pokemon.ownerUUID != player.uuid ||
            !pokemon.isAlive ||
            pokemon.isBattling ||
            pokemon.distanceToSqr(player) > MAX_INTERACTION_DISTANCE_SQUARED ||
            player.isPassenger ||
            player.passengers.isNotEmpty()
        ) {
            player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.invalid"))
            return
        }

        pokemon.addTag(ATTACHED_TAG)
        pokemon.setNoGravity(true)
        pokemon.setNoAi(true)
        pokemon.setInvulnerable(true)
        if (!pokemon.startRiding(player, true)) {
            restorePokemon(pokemon)
            player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.invalid"))
            return
        }
        player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.attached"))
    }

    fun tickWorld(world: ServerLevel) {
        for (player in world.players()) {
            val buddy = findBuddy(player) ?: continue
            if (!buddy.isAlive || buddy.level() != player.level()) {
                release(player)
                continue
            }
            if (!buddy.isPassenger && !buddy.startRiding(player, true)) {
                release(player)
                continue
            }

            buddy.setNoGravity(true)
            buddy.setNoAi(true)
            buddy.setInvulnerable(true)
            buddy.deltaMovement = Vec3.ZERO

            if (!player.onGround() &&
                player.deltaMovement.y < -0.035 &&
                !player.isInWaterOrBubble &&
                !player.isFallFlying
            ) {
                player.startFallFlying()
            }
        }
    }

    fun release(player: ServerPlayer, notifyPlayer: Boolean = true) {
        val buddy = findBuddy(player)
        if (buddy != null) {
            buddy.stopRiding()
            restorePokemon(buddy)
            buddy.teleportTo(player.x, player.y + 0.05, player.z)
        }
        if (player.isFallFlying) player.stopFallFlying()
        if (notifyPlayer && buddy != null) {
            player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.detached"))
        }
    }

    private fun findBuddy(player: Entity): PokemonEntity? =
        player.passengers.filterIsInstance<PokemonEntity>().firstOrNull { it.tags.contains(ATTACHED_TAG) }

    private fun restorePokemon(pokemon: PokemonEntity) {
        pokemon.removeTag(ATTACHED_TAG)
        pokemon.setNoGravity(false)
        pokemon.setNoAi(false)
        pokemon.setInvulnerable(false)
        pokemon.deltaMovement = Vec3.ZERO
    }
}
