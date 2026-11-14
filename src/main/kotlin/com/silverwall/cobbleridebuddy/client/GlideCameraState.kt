package com.silverwall.cobbleridebuddy.client

import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag

object GlideCameraState {
    private var previousCameraType: CameraType? = null
    var active: Boolean = false
        private set
    private var fovBlend: Float = 0.0f

    fun tick(client: Minecraft) {
        val player = client.player
        val hasCrobatBuddy = player?.let {
            isCrobatShoulder(it.shoulderEntityLeft) || isCrobatShoulder(it.shoulderEntityRight)
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

    private fun isCrobatShoulder(tag: CompoundTag): Boolean =
        tag.getString(DataKeys.SHOULDER_SPECIES) == "cobblemon:crobat"

    @JvmStatic
    fun cinematicFovMultiplier(): Double = 1.0 + 0.065 * fovBlend.toDouble()
}
