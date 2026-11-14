package com.silverwall.cobbleridebuddy.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * Requests a skill activation for a sent-out Pokémon, or deactivation of the
 * active companion skill when [deactivate] is true.
 */
data class CompanionSkillPayload(
    val skillId: ResourceLocation,
    val targetPokemonUuid: UUID?,
    val deactivate: Boolean
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "companion_skill")
        val TYPE = CustomPacketPayload.Type<CompanionSkillPayload>(ID)

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, CompanionSkillPayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeResourceLocation(payload.skillId)
                buffer.writeBoolean(payload.deactivate)
                buffer.writeBoolean(payload.targetPokemonUuid != null)
                payload.targetPokemonUuid?.let(buffer::writeUUID)
            },
            { buffer ->
                val skillId = buffer.readResourceLocation()
                val deactivate = buffer.readBoolean()
                val target = if (buffer.readBoolean()) buffer.readUUID() else null
                CompanionSkillPayload(skillId, target, deactivate)
            }
        )
    }
}
