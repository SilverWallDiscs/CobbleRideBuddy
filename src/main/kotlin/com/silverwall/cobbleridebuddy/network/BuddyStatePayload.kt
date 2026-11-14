package com.silverwall.cobbleridebuddy.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class BuddyStatePayload(
    val active: Boolean,
    val power: Float,
    val maxPower: Float
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "buddy_state")
        val TYPE = CustomPacketPayload.Type<BuddyStatePayload>(ID)
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, BuddyStatePayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeBoolean(payload.active)
                buffer.writeFloat(payload.power)
                buffer.writeFloat(payload.maxPower)
            },
            { buffer -> BuddyStatePayload(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat()) }
        )
    }
}
