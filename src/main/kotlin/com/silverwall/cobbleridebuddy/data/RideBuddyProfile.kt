package com.silverwall.cobbleridebuddy.data

data class RideBuddyProfile(
    val pokemon: String,
    val skill: String,
    val maxPower: Float,
    val powerGainPerTick: Float,
    val powerDecayPerTick: Float,
    val thrustPerPower: Double,
    val maxHorizontalSpeed: Double
) {
    companion object {
        val DEFAULT_CROBAT = RideBuddyProfile(
            pokemon = "cobblemon:crobat",
            skill = "cobbleridebuddy:ride_buddy",
            maxPower = 10.0f,
            powerGainPerTick = 0.04f,
            powerDecayPerTick = 0.02f,
            thrustPerPower = 0.00012,
            maxHorizontalSpeed = 2.2
        )
    }
}
