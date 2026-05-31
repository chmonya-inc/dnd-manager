package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity

sealed interface CharacterCreateEvent {
    // Basic info
    data class NameChanged(val value: String) : CharacterCreateEvent
    data class PlayerNameChanged(val value: String) : CharacterCreateEvent
    data class RaceChanged(val value: String) : CharacterCreateEvent
    data class ClassChanged(val value: String) : CharacterCreateEvent
    data class SubclassChanged(val value: String) : CharacterCreateEvent
    data class BackgroundChanged(val value: String) : CharacterCreateEvent
    data class LevelChanged(val value: String) : CharacterCreateEvent
    data class ExperiencePointsChanged(val value: String) : CharacterCreateEvent
    data class DescriptionChanged(val value: String) : CharacterCreateEvent
    data class ImageUrlChanged(val value: String) : CharacterCreateEvent

    // Appearance
    data class AgeChanged(val value: String) : CharacterCreateEvent
    data class GenderChanged(val value: String) : CharacterCreateEvent
    data class HeightChanged(val value: String) : CharacterCreateEvent
    data class WeightChanged(val value: String) : CharacterCreateEvent
    data class EyesChanged(val value: String) : CharacterCreateEvent
    data class HairChanged(val value: String) : CharacterCreateEvent
    data class SkinChanged(val value: String) : CharacterCreateEvent

    // Ability Scores
    data class StrengthChanged(val value: String) : CharacterCreateEvent
    data class DexterityChanged(val value: String) : CharacterCreateEvent
    data class ConstitutionChanged(val value: String) : CharacterCreateEvent
    data class IntelligenceChanged(val value: String) : CharacterCreateEvent
    data class WisdomChanged(val value: String) : CharacterCreateEvent
    data class CharismaChanged(val value: String) : CharacterCreateEvent

    // HP & Combat
    data class MaxHpChanged(val value: String) : CharacterCreateEvent
    data class CurrentHpChanged(val value: String) : CharacterCreateEvent
    data class TempHpChanged(val value: String) : CharacterCreateEvent
    data class ArmorClassChanged(val value: String) : CharacterCreateEvent
    data class InitiativeChanged(val value: String) : CharacterCreateEvent
    data class SpeedChanged(val value: String) : CharacterCreateEvent
    data class ProficiencyBonusChanged(val value: String) : CharacterCreateEvent
    data class HitDiceChanged(val value: String) : CharacterCreateEvent
    data class HitDiceCurrentChanged(val value: String) : CharacterCreateEvent

    // Status
    data class InspirationChanged(val value: Boolean) : CharacterCreateEvent
    data class ExhaustionChanged(val value: String) : CharacterCreateEvent
    data class ConditionsChanged(val value: String) : CharacterCreateEvent
    data class DeathSaveSuccessesChanged(val value: String) : CharacterCreateEvent
    data class DeathSaveFailuresChanged(val value: String) : CharacterCreateEvent

    // Proficiencies
    data class SavingThrowsChanged(val value: String) : CharacterCreateEvent
    data class SkillsChanged(val value: String) : CharacterCreateEvent
    data class ArmorProficienciesChanged(val value: String) : CharacterCreateEvent
    data class WeaponProficienciesChanged(val value: String) : CharacterCreateEvent
    data class ToolProficienciesChanged(val value: String) : CharacterCreateEvent
    data class LanguagesChanged(val value: String) : CharacterCreateEvent

    // Items
    data object AddItem : CharacterCreateEvent
    data class RemoveItem(val index: Int) : CharacterCreateEvent
    data class ItemNameChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class ItemSlotChanged(val index: Int, val value: EquipmentSlot?) : CharacterCreateEvent
    data class ItemRarityChanged(val index: Int, val value: ItemRarity) : CharacterCreateEvent
    data class ItemDescriptionChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class ItemEquippedChanged(val index: Int, val value: Boolean) : CharacterCreateEvent

    // Weapons
    data object AddWeapon : CharacterCreateEvent
    data class RemoveWeapon(val index: Int) : CharacterCreateEvent
    data class WeaponNameChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class WeaponAttackBonusChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class WeaponDamageChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class WeaponDamageTypeChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class WeaponNotesChanged(val index: Int, val value: String) : CharacterCreateEvent

    // Features
    data class ClassFeaturesChanged(val value: String) : CharacterCreateEvent
    data class RacialTraitsChanged(val value: String) : CharacterCreateEvent
    data class FeatsChanged(val value: String) : CharacterCreateEvent

    data object SaveCharacter : CharacterCreateEvent
}
