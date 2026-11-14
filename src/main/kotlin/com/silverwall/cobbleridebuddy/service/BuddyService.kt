package com.silverwall.cobbleridebuddy.service

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.silverwall.cobbleridebuddy.data.RideBuddyDataLoader
import com.silverwall.cobbleridebuddy.network.BuddyControlPayload
import com.silverwall.cobbleridebuddy.network.BuddyStatePayload
import com.silverwall.cobbleridebuddy.network.CompanionSkillPayload
import com.silverwall.cobbleridebuddy.skill.CompanionSkillRegistry
import com.silverwall.cobbleridebuddy.skill.RideBuddySkill
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sqrt

object BuddyService {
    private data class ControlState(var forwardHeld: Boolean = false)

    private val controls = ConcurrentHashMap<UUID, ControlState>()
    private val powerByPlayer = ConcurrentHashMap<UUID, Float>()
    private val lastSentState = ConcurrentHashMap<UUID, Boolean>()

    fun handleSkillRequest(player: ServerPlayer, payload: CompanionSkillPayload) {
        val skill = CompanionSkillRegistry.get(payload.skillId)
        if (skill == null) {
            player.sendSystemMessage(Component.translatable("message.cobbleridebuddy.skill_unknown"))
            return
        }

        val succeeded = if (payload.deactivate) {
            skill.deactivate(player)
        } else {
            val target = payload.targetPokemonUuid?.let {
                player.serverLevel().getEntity(it) as? PokemonEntity
            }
            skill.activate(player, target)
        }

        if (!succeeded) {
            player.sendSystemMessage(
                Component.translatable(
                    if (payload.deactivate) "message.cobbleridebuddy.skill_not_active"
                    else "message.cobbleridebuddy.skill_unavailable"
                )
            )
        } else if (payload.deactivate && skill.id == RideBuddySkill.id) {
            powerByPlayer.remove(player.uuid)
            if (player.isFallFlying) player.stopFallFlying()
            sendState(player, false, 0.0f, RideBuddySkill.CROBAT_ID)
        }
    }

    fun handleControl(player: ServerPlayer, payload: BuddyControlPayload) {
        controls.computeIfAbsent(player.uuid) { ControlState() }.forwardHeld = payload.forwardHeld

        if (payload.startGliding &&
            RideBuddySkill.findAttachedPokemon(player) != null &&
            !player.onGround() &&
            !player.isInWaterOrBubble &&
            player.isAlive &&
            !player.isFallFlying
        ) {
            // This is the server-side equivalent of vanilla's START_FALL_FLYING
            // action, intentionally available without an Elytra item.
            player.startFallFlying()
        }
    }

    fun tickWorld(world: ServerLevel) {
        for (player in world.players()) {
            val buddy = RideBuddySkill.findAttachedPokemon(player)
            if (buddy == null) {
                val wasActive = lastSentState.remove(player.uuid) == true
                powerByPlayer.remove(player.uuid)
                if (wasActive) {
                    sendState(player, false, 0.0f, RideBuddySkill.CROBAT_ID)
                }
                continue
            }

            val profile = RideBuddyDataLoader.get(buddy.species.resourceIdentifier.toString())
            val input = controls[player.uuid]
            var power = powerByPlayer[player.uuid] ?: 0.0f

            if (player.onGround() || player.isInWaterOrBubble) {
                power = 0.0f
                if (player.isFallFlying) player.stopFallFlying()
            } else if (player.isFallFlying && input?.forwardHeld == true) {
                power = (power + profile.powerGainPerTick).coerceAtMost(profile.maxPower)
                applyPropulsion(player, power, profile.thrustPerPower, profile.maxHorizontalSpeed)
            } else {
                power = (power - profile.powerDecayPerTick).coerceAtLeast(0.0f)
            }

            powerByPlayer[player.uuid] = power
            if (player.tickCount % 4 == 0) {
                sendState(player, true, power, profile.pokemon, profile.maxPower)
            }
        }
    }

    private fun applyPropulsion(
        player: ServerPlayer,
        power: Float,
        thrustPerPower: Double,
        maxHorizontalSpeed: Double
    ) {
        if (power <= 0.0f) return

        val velocity = player.deltaMovement
        val horizontalSpeed = sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
        val remainingSpeed = (maxHorizontalSpeed - horizontalSpeed).coerceAtLeast(0.0)
        if (remainingSpeed <= 0.0) return

        val look = player.lookAngle
        val direction = Vec3(look.x, 0.0, look.z)
        if (direction.lengthSqr() < 1.0E-6) return

        val acceleration = (thrustPerPower * power).coerceAtMost(remainingSpeed)
        player.deltaMovement = velocity.add(direction.normalize().scale(acceleration))
    }

    private fun sendState(
        player: ServerPlayer,
        active: Boolean,
        power: Float,
        pokemonId: String,
        maxPower: Float = RideBuddyDataLoader.get(pokemonId).maxPower
    ) {
        lastSentState[player.uuid] = active
        ServerPlayNetworking.send(player, BuddyStatePayload(active, power, maxPower))
    }

    fun clearSession(player: ServerPlayer) {
        controls.remove(player.uuid)
        powerByPlayer.remove(player.uuid)
        lastSentState.remove(player.uuid)
        // Deliberately do not recall the Pokémon: Cobblemon persists its native
        // ShoulderedState and shoulder NBT across disconnects.
    }
}
