package com.dnd.helper.domain.model

/**
 * All 18 D&D 5e skills and their associated ability scores.
 */
enum class DndSkill(
    val displayName: String,
    val ability: String,
) {
    ATHLETICS("Athletics", "strength"),

    ACROBATICS("Acrobatics", "dexterity"),
    SLEIGHT_OF_HAND("Sleight of Hand", "dexterity"),
    STEALTH("Stealth", "dexterity"),

    ARCANA("Arcana", "intelligence"),
    HISTORY("History", "intelligence"),
    INVESTIGATION("Investigation", "intelligence"),
    NATURE("Nature", "intelligence"),
    RELIGION("Religion", "intelligence"),

    ANIMAL_HANDLING("Animal Handling", "wisdom"),
    INSIGHT("Insight", "wisdom"),
    MEDICINE("Medicine", "wisdom"),
    PERCEPTION("Perception", "wisdom"),
    SURVIVAL("Survival", "wisdom"),

    DECEPTION("Deception", "charisma"),
    INTIMIDATION("Intimidation", "charisma"),
    PERFORMANCE("Performance", "charisma"),
    PERSUASION("Persuasion", "charisma"),
    ;

    companion object {
        fun byAbility(ability: String): List<DndSkill> =
            entries.filter { it.ability == ability.lowercase() }

        fun all(): List<DndSkill> = entries
    }
}
