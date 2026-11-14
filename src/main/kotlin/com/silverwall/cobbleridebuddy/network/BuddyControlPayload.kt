package com.silverwall.cobbleridebuddy.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class BuddyControlPayload(
    val startGliding: Boolean,
    val forwardHeld: Boolean
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "buddy_control")
        val TYPE = CustomPacketPayload.Type<BuddyControlPayload>(ID)
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, BuddyControlPayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeBoolean(payload.startGliding)
                buffer.writeBoolean(payload.forwardHeld)
            },
            { buffer -> BuddyControlPayload(buffer.readBoolean(), buffer.readBoolean()) }
        )
    }
}
