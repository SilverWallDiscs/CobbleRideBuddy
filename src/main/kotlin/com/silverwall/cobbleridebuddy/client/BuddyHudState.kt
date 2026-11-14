package com.silverwall.cobbleridebuddy.client

object BuddyHudState {
    @Volatile var active: Boolean = false
        private set
    @Volatile var power: Float = 0.0f
        private set
    @Volatile var maxPower: Float = 10.0f
        private set

    fun update(active: Boolean, power: Float, maxPower: Float) {
        this.active = active
        this.power = power.coerceAtLeast(0.0f)
        this.maxPower = maxPower.coerceAtLeast(1.0f)
    }
}
