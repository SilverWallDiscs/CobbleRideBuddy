package com.silverwall.cobbleridebuddy.client

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft

object GlideCameraState {
    private var previousCameraType: CameraType? = null
    var active: Boolean = false
        private set
    private var fovBlend: Float = 0.0f

    fun tick(client: Minecraft) {
        val player = client.player
        val hasCrobatBuddy = player?.passengers?.any { passenger ->
            passenger is PokemonEntity &&
                passenger.pokemon.species.resourceIdentifier.toString() == "cobblemon:crobat"
        } == true
        val shouldUseCinematicCamera = hasCrobatBuddy && player?.isFallFlying == true

        if (shouldUseCinematicCamera && !active) {
            previousCameraType = client.options.cameraType
            client.options.cameraType = CameraType.THIRD_PERSON_BACK
        } else if (!shouldUseCinematicCamera && active) {
            previousCameraType?.let { client.options.cameraType = it }
            previousCameraType = null
        }

        active = shouldUseCinematicCamera
        val targetBlend = if (active) 1.0f else 0.0f
        fovBlend += (targetBlend - fovBlend) * 0.14f
    }

    @JvmStatic
    fun cinematicFovMultiplier(): Double = 1.0 + 0.065 * fovBlend.toDouble()
}
