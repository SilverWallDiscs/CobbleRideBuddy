package com.silverwall.cobbleridebuddy.client

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.silverwall.cobbleridebuddy.network.BuddyControlPayload
import com.silverwall.cobbleridebuddy.network.BuddyStatePayload
import com.silverwall.cobbleridebuddy.network.CompanionSkillPayload
import com.silverwall.cobbleridebuddy.skill.RideBuddySkill
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

object CobbleRideBuddyClient : ClientModInitializer {
    private lateinit var openSkillsKey: KeyMapping
    private var lastForwardHeld = false
    private var lastJumpHeld = false
    private var controlTicks = 0

    override fun onInitializeClient() {
        openSkillsKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.cobbleridebuddy.open_skills",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.category.cobbleridebuddy"
            )
        )

        // Reuse Cobblemon's Pokémon radial menu. Replace the vanilla generic
        // shoulder action for Crobat with the extensible Ride Buddy Skill action.
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
                target.pokemon.species.resourceIdentifier.toString() == RideBuddySkill.CROBAT_ID &&
                target.ownerUUID == player.uuid
            ) {
                event.options.entries().removeIf { it.value.tooltipText == "cobblemon.ui.interact.mount.shoulder" }
                event.addFillingOption(
                    InteractWheelOption(
                        iconResource = ResourceLocation.fromNamespaceAndPath(
                            "cobblemon",
                            "textures/gui/interact/icon_shoulder.png"
                        ),
                        tooltipText = "cobbleridebuddy.skill.ride_buddy",
                        onPress = {
                            ClientPlayNetworking.send(
                                CompanionSkillPayload(
                                    skillId = RideBuddySkill.id,
                                    targetPokemonUuid = target.uuid,
                                    deactivate = false
                                )
                            )
                        }
                    )
                )
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(BuddyStatePayload.TYPE) { payload, context ->
            context.client().execute {
                BuddyHudState.update(payload.active, payload.power, payload.maxPower)
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.screen == null) {
                if (openSkillsKey.consumeClick()) {
                    client.setScreen(CompanionSkillsScreen())
                }

                val forwardHeld = client.options.keyUp.isDown
                // Observe SPACE as a key state edge instead of consuming the
                // vanilla jump mapping's click queue. This lets normal jumping
                // keep working while a fresh airborne press starts the glide.
                val jumpHeld = client.options.keyJump.isDown
                val startGliding = jumpHeld && !lastJumpHeld

                if (startGliding ||
                    forwardHeld != lastForwardHeld ||
                    (forwardHeld && controlTicks % 4 == 0)
                ) {
                    ClientPlayNetworking.send(BuddyControlPayload(startGliding, forwardHeld))
                }
                lastForwardHeld = forwardHeld
                lastJumpHeld = jumpHeld
                controlTicks++
            } else {
                if (lastForwardHeld && client.player != null) {
                    ClientPlayNetworking.send(BuddyControlPayload(false, false))
                }
                lastForwardHeld = false
                lastJumpHeld = false
            }

            GlideCameraState.tick(client)
        }

        HudRenderCallback.EVENT.register { graphics, _ ->
            if (BuddyHudState.active) {
                val client = Minecraft.getInstance()
                if (client.player?.isFallFlying == true) {
                    val power = BuddyHudState.power.coerceIn(0.0f, BuddyHudState.maxPower)
                    val fraction = (power / BuddyHudState.maxPower).coerceIn(0.0f, 1.0f)
                    val barWidth = 82
                    val x = 8
                    val y = client.window.guiScaledHeight - 34
                    graphics.drawString(
                        client.font,
                        Component.translatable("hud.cobbleridebuddy.power", power.toInt(), BuddyHudState.maxPower.toInt()),
                        x,
                        y - 12,
                        0xFFFFFF
                    )
                    graphics.fill(x, y, x + barWidth, y + 6, 0xAA202020.toInt())
                    graphics.fill(x, y, x + (barWidth * fraction).toInt(), y + 6, 0xFF7CC7FF.toInt())
                }
            }
        }
    }
}
