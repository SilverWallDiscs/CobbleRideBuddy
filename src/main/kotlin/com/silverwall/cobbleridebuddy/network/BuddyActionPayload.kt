package com.silverwall.cobbleridebuddy.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/** A null UUID asks the server to release the currently attached buddy. */
data class BuddyActionPayload(val pokemonUuid: UUID?) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "buddy_action")
        val TYPE = CustomPacketPayload.Type<BuddyActionPayload>(ID)
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, BuddyActionPayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeBoolean(payload.pokemonUuid != null)
                payload.pokemonUuid?.let(buffer::writeUUID)
            },
            { buffer -> BuddyActionPayload(if (buffer.readBoolean()) buffer.readUUID() else null) }
        )
    }
}
