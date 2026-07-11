package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity

sealed interface PlayerCharacterCreateEvent {
    // Basic info
    data class LoadCharacter(val characterId: String) : PlayerCharacterCreateEvent
    data class NameChanged(val value: String) : PlayerCharacterCreateEvent
    data class PlayerNameChanged(val value: String) : PlayerCharacterCreateEvent
    data class RaceChanged(val value: String) : PlayerCharacterCreateEvent
    data class SubraceChanged(val value: String) : PlayerCharacterCreateEvent
    data class ClassChanged(val value: String) : PlayerCharacterCreateEvent
    data class SubclassChanged(val value: String) : PlayerCharacterCreateEvent
    data class BackgroundChanged(val value: String) : PlayerCharacterCreateEvent
    data class AlignmentChanged(val value: String) : PlayerCharacterCreateEvent
    data class LevelChanged(val value: String) : PlayerCharacterCreateEvent
    data class ExperiencePointsChanged(val value: String) : PlayerCharacterCreateEvent
    data class DescriptionChanged(val value: String) : PlayerCharacterCreateEvent
    data class ImageUrlChanged(val value: String) : PlayerCharacterCreateEvent

    // Appearance
    data class AgeChanged(val value: String) : PlayerCharacterCreateEvent
    data class GenderChanged(val value: String) : PlayerCharacterCreateEvent
    data class HeightChanged(val value: String) : PlayerCharacterCreateEvent
    data class WeightChanged(val value: String) : PlayerCharacterCreateEvent
    data class EyesChanged(val value: String) : PlayerCharacterCreateEvent
    data class HairChanged(val value: String) : PlayerCharacterCreateEvent
    data class SkinChanged(val value: String) : PlayerCharacterCreateEvent

    // Ability Scores
    data class StrengthChanged(val value: String) : PlayerCharacterCreateEvent
    data class DexterityChanged(val value: String) : PlayerCharacterCreateEvent
    data class ConstitutionChanged(val value: String) : PlayerCharacterCreateEvent
    data class IntelligenceChanged(val value: String) : PlayerCharacterCreateEvent
    data class WisdomChanged(val value: String) : PlayerCharacterCreateEvent
    data class CharismaChanged(val value: String) : PlayerCharacterCreateEvent

    // HP & Combat
    data class MaxHpChanged(val value: String) : PlayerCharacterCreateEvent
    data class CurrentHpChanged(val value: String) : PlayerCharacterCreateEvent
    data class TempHpChanged(val value: String) : PlayerCharacterCreateEvent
    data class ArmorClassChanged(val value: String) : PlayerCharacterCreateEvent
    data class InitiativeChanged(val value: String) : PlayerCharacterCreateEvent
    data class SpeedChanged(val value: String) : PlayerCharacterCreateEvent
    data class ProficiencyBonusChanged(val value: String) : PlayerCharacterCreateEvent
    data class HitDiceChanged(val value: String) : PlayerCharacterCreateEvent
    data class HitDiceCurrentChanged(val value: String) : PlayerCharacterCreateEvent

    // Status
    data class InspirationChanged(val value: Boolean) : PlayerCharacterCreateEvent
    data class ExhaustionChanged(val value: String) : PlayerCharacterCreateEvent
    data class ConditionsChanged(val value: String) : PlayerCharacterCreateEvent
    data class DeathSaveSuccessesChanged(val value: String) : PlayerCharacterCreateEvent
    data class DeathSaveFailuresChanged(val value: String) : PlayerCharacterCreateEvent

    // Proficiencies
    data class SavingThrowsChanged(val value: String) : PlayerCharacterCreateEvent
    data class ArmorProficienciesChanged(val value: String) : PlayerCharacterCreateEvent

    // Multi-select actions
    data class AddLanguage(val value: String) : PlayerCharacterCreateEvent
    data class RemoveLanguage(val value: String) : PlayerCharacterCreateEvent
    data class AddProficiencySkill(val value: String) : PlayerCharacterCreateEvent
    data class RemoveProficiencySkill(val value: String) : PlayerCharacterCreateEvent
    data class AddProficiencyWeapon(val value: String) : PlayerCharacterCreateEvent
    data class RemoveProficiencyWeapon(val value: String) : PlayerCharacterCreateEvent
    data class AddProficiencyTool(val value: String) : PlayerCharacterCreateEvent
    data class RemoveProficiencyTool(val value: String) : PlayerCharacterCreateEvent
    data class AddClassFeature(val value: String) : PlayerCharacterCreateEvent
    data class RemoveClassFeature(val value: String) : PlayerCharacterCreateEvent
    data class AddRacialTrait(val value: String) : PlayerCharacterCreateEvent
    data class RemoveRacialTrait(val value: String) : PlayerCharacterCreateEvent
    data class AddFeat(val value: String) : PlayerCharacterCreateEvent
    data class RemoveFeat(val value: String) : PlayerCharacterCreateEvent

    // Items
    data object AddItem : PlayerCharacterCreateEvent
    data class RemoveItem(val index: Int) : PlayerCharacterCreateEvent
    data class ItemNameChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class ItemSlotChanged(val index: Int, val value: EquipmentSlot?) : PlayerCharacterCreateEvent
    data class ItemRarityChanged(val index: Int, val value: ItemRarity) : PlayerCharacterCreateEvent
    data class ItemDescriptionChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class ItemImageUrlChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class ItemEquippedChanged(val index: Int, val value: Boolean) : PlayerCharacterCreateEvent

    // Weapons
    data object AddWeapon : PlayerCharacterCreateEvent
    data class RemoveWeapon(val index: Int) : PlayerCharacterCreateEvent
    data class WeaponNameChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class WeaponAttackBonusChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class WeaponDamageChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class WeaponDamageTypeChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class WeaponNotesChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent

    // Spells
    data object AddSpell : PlayerCharacterCreateEvent
    data class RemoveSpell(val index: Int) : PlayerCharacterCreateEvent
    data class SpellNameChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellDescriptionChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellLevelChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellSchoolChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellCastingTimeChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellRangeChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellDurationChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellDamageChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent
    data class SpellDamageTypeChanged(val index: Int, val value: String) : PlayerCharacterCreateEvent

    // Features

    data object SaveCharacter : PlayerCharacterCreateEvent
    data object DismissError : PlayerCharacterCreateEvent

    data object GenerateImage : PlayerCharacterCreateEvent
    data class GenerateItemImage(val index: Int) : PlayerCharacterCreateEvent
    data class AiSizeChanged(val width: Int, val height: Int) : PlayerCharacterCreateEvent
    data class AiPromptChanged(val value: String) : PlayerCharacterCreateEvent
}
