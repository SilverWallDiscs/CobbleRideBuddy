package com.silverwall.cobbleridebuddy

import com.silverwall.cobbleridebuddy.data.RideBuddyDataLoader
import com.silverwall.cobbleridebuddy.network.BuddyControlPayload
import com.silverwall.cobbleridebuddy.network.BuddyStatePayload
import com.silverwall.cobbleridebuddy.network.CompanionSkillPayload
import com.silverwall.cobbleridebuddy.service.BuddyService
import com.silverwall.cobbleridebuddy.skill.CompanionSkillRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType

object CobbleRideBuddy : ModInitializer {
    override fun onInitialize() {
        CompanionSkillRegistry.registerBuiltInSkills()
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RideBuddyDataLoader)

        PayloadTypeRegistry.playC2S().register(CompanionSkillPayload.TYPE, CompanionSkillPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(BuddyControlPayload.TYPE, BuddyControlPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(BuddyStatePayload.TYPE, BuddyStatePayload.CODEC)

        ServerPlayNetworking.registerGlobalReceiver(CompanionSkillPayload.TYPE) { payload, context ->
            BuddyService.handleSkillRequest(context.player(), payload)
        }
        ServerPlayNetworking.registerGlobalReceiver(BuddyControlPayload.TYPE) { payload, context ->
            BuddyService.handleControl(context.player(), payload)
        }

        ServerTickEvents.END_WORLD_TICK.register { world -> BuddyService.tickWorld(world) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            BuddyService.clearSession(handler.player)
        }
    }
}
