package com.silverwall.cobbleridebuddy

import com.silverwall.cobbleridebuddy.network.BuddyActionPayload
import com.silverwall.cobbleridebuddy.service.BuddyService
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object CobbleRideBuddy : ModInitializer {
    override fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(BuddyActionPayload.TYPE, BuddyActionPayload.CODEC)
        ServerPlayNetworking.registerGlobalReceiver(BuddyActionPayload.TYPE) { payload, context ->
            BuddyService.handleAction(context.player(), payload)
        }
        ServerTickEvents.END_WORLD_TICK.register { world -> BuddyService.tickWorld(world) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            BuddyService.release(handler.player, notifyPlayer = false)
        }
    }
}
