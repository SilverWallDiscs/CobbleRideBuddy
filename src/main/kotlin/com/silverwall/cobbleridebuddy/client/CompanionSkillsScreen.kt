package com.silverwall.cobbleridebuddy.client

import com.silverwall.cobbleridebuddy.network.CompanionSkillPayload
import com.silverwall.cobbleridebuddy.skill.RideBuddySkill
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class CompanionSkillsScreen : Screen(Component.translatable("screen.cobbleridebuddy.skills")) {
    override fun init() {
        val releaseButton = Button.builder(
            Component.translatable("screen.cobbleridebuddy.ride_buddy.release")
        ) {
            if (BuddyHudState.active) {
                ClientPlayNetworking.send(
                    CompanionSkillPayload(
                        skillId = RideBuddySkill.id,
                        targetPokemonUuid = null,
                        deactivate = true
                    )
                )
            }
            minecraft?.setScreen(null)
        }.bounds(width / 2 - 100, height / 2 + 8, 200, 20).build()

        releaseButton.active = BuddyHudState.active
        addRenderableWidget(releaseButton)

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                minecraft?.setScreen(null)
            }.bounds(width / 2 - 100, height / 2 + 36, 200, 20).build()
        )
    }

    override fun render(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick)
        super.render(graphics, mouseX, mouseY, partialTick)

        graphics.drawCenteredString(font, title, width / 2, height / 2 - 48, 0xFFFFFF)
        val status = if (BuddyHudState.active) {
            Component.translatable(
                "screen.cobbleridebuddy.status.active",
                BuddyHudState.power.toInt(),
                BuddyHudState.maxPower.toInt()
            )
        } else {
            Component.translatable("screen.cobbleridebuddy.status.inactive")
        }
        graphics.drawCenteredString(font, status, width / 2, height / 2 - 20, 0xD0D0D0)
        graphics.drawCenteredString(
            font,
            Component.translatable("screen.cobbleridebuddy.help"),
            width / 2,
            height / 2 - 4,
            0xA0A0A0
        )
    }

    override fun isPauseScreen(): Boolean = false
}
