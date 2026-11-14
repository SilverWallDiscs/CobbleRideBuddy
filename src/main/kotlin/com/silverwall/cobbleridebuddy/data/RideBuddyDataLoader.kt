package com.silverwall.cobbleridebuddy.data

import com.google.gson.JsonParser
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.ConcurrentHashMap

object RideBuddyDataLoader : SimpleSynchronousResourceReloadListener {
    private val profiles = ConcurrentHashMap<String, RideBuddyProfile>()

    override fun getFabricId(): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath("cobbleridebuddy", "ride_buddy_profiles")

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        val updated = ConcurrentHashMap<String, RideBuddyProfile>()
        val resources = resourceManager.listResources("ride_buddy_skills") { id ->
            id.path.endsWith(".json")
        }

        resources.values.forEach { resource ->
            runCatching {
                resource.open().bufferedReader().use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    val profile = RideBuddyProfile(
                        pokemon = json.get("pokemon").asString,
                        skill = json.get("skill").asString,
                        maxPower = json.get("maxPower").asFloat,
                        powerGainPerTick = json.get("powerGainPerTick").asFloat,
                        powerDecayPerTick = json.get("powerDecayPerTick").asFloat,
                        thrustPerPower = json.get("thrustPerPower").asDouble,
                        maxHorizontalSpeed = json.get("maxHorizontalSpeed").asDouble
                    )
                    require(profile.maxPower > 0f)
                    require(profile.powerGainPerTick >= 0f && profile.powerDecayPerTick >= 0f)
                    require(profile.thrustPerPower >= 0.0 && profile.maxHorizontalSpeed > 0.0)
                    updated[profile.pokemon] = profile
                }
            }.onFailure { error ->
                System.err.println("[CobbleRideBuddy] Could not load a ride buddy profile: ${error.message}")
            }
        }

        profiles.clear()
        profiles.putAll(updated)
        if (!profiles.containsKey(RideBuddyProfile.DEFAULT_CROBAT.pokemon)) {
            profiles[RideBuddyProfile.DEFAULT_CROBAT.pokemon] = RideBuddyProfile.DEFAULT_CROBAT
        }
    }

    fun get(pokemonId: String): RideBuddyProfile =
        profiles[pokemonId] ?: RideBuddyProfile.DEFAULT_CROBAT
}
