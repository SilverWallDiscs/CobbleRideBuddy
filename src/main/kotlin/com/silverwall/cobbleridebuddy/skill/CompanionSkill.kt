package com.silverwall.cobbleridebuddy.skill

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

interface CompanionSkill {
    val id: ResourceLocation

    fun activate(player: ServerPlayer, target: PokemonEntity?): Boolean
    fun deactivate(player: ServerPlayer): Boolean
}

object CompanionSkillRegistry {
    private val skills = linkedMapOf<ResourceLocation, CompanionSkill>()

    fun register(skill: CompanionSkill) {
        skills[skill.id] = skill
    }

    fun get(id: ResourceLocation): CompanionSkill? = skills[id]

    fun registerBuiltInSkills() {
        register(RideBuddySkill)
    }
}
