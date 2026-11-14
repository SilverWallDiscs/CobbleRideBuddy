package com.silverwall.cobbleridebuddy.client

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.silverwall.cobbleridebuddy.network.BuddyActionPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

object CobbleRideBuddyClient : ClientModInitializer {
    private lateinit var releaseBuddyKey: KeyMapping

    override fun onInitializeClient() {
        releaseBuddyKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.cobbleridebuddy.release",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.category.cobbleridebuddy"
            )
        )

        // Extend Cobblemon's native Shift + right-click Pokémon interaction wheel.
        // This is also the wheel used by Cobbleverse's Pokémon actions.
        CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.subscribe { event ->
            val client = Minecraft.getInstance()
            val player = client.player
            val target = if (client.level != null && player != null) {
                client.level!!.getEntitiesOfClass(
                    PokemonEntity::class.java,
                    player.boundingBox.inflate(16.0)
                ).firstOrNull { it.uuid == event.pokemonID }
            } else {
                null
            }

            if (player != null &&
                target != null &&
                target.pokemon.species.resourceIdentifier.toString() == "cobblemon:crobat" &&
                target.ownerUUID == player.uuid
            ) {
                event.addFillingOption(
                    InteractWheelOption(
                        iconResource = ResourceLocation.fromNamespaceAndPath(
                            "cobblemon",
                            "textures/gui/interact/icon_shoulder.png"
                        ),
                        tooltipText = "cobbleridebuddy.ui.attach",
                        onPress = {
                            ClientPlayNetworking.send(BuddyActionPayload(target.uuid))
                        }
                    )
                )
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (releaseBuddyKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(BuddyActionPayload(null))
                }
            }
            GlideCameraState.tick(client)
        }
    }
}
