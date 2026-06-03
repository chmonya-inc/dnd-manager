package com.dnd.helper.presentation.monstercreate

sealed interface MonsterCreateEvent {
    data class NameChanged(val value: String) : MonsterCreateEvent
    data class DescriptionChanged(val value: String) : MonsterCreateEvent
    data class ChallengeRatingChanged(val value: String) : MonsterCreateEvent
    data class TypeChanged(val value: String) : MonsterCreateEvent
    data class AlignmentChanged(val value: String) : MonsterCreateEvent
    data class SizeChanged(val value: String) : MonsterCreateEvent
    data class MaxHpChanged(val value: String) : MonsterCreateEvent
    data class ArmorClassChanged(val value: String) : MonsterCreateEvent
    data class SpeedChanged(val value: String) : MonsterCreateEvent
    
    data class StrengthChanged(val value: String) : MonsterCreateEvent
    data class DexterityChanged(val value: String) : MonsterCreateEvent
    data class ConstitutionChanged(val value: String) : MonsterCreateEvent
    data class IntelligenceChanged(val value: String) : MonsterCreateEvent
    data class WisdomChanged(val value: String) : MonsterCreateEvent
    data class CharismaChanged(val value: String) : MonsterCreateEvent
    
    data class AddLanguage(val item: String) : MonsterCreateEvent
    data class RemoveLanguage(val item: String) : MonsterCreateEvent
    
    data class AddSpecialAbility(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    data class RemoveSpecialAbility(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    
    data class AddAction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    data class RemoveAction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    
    data class AddLegendaryAction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    data class RemoveLegendaryAction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    
    data class AddReaction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    data class RemoveReaction(val action: com.dnd.helper.domain.model.MonsterAction) : MonsterCreateEvent
    
    data class AddConditionImmunity(val value: String) : MonsterCreateEvent
    data class RemoveConditionImmunity(val value: String) : MonsterCreateEvent
    
    data class AddDamageImmunity(val value: String) : MonsterCreateEvent
    data class RemoveDamageImmunity(val value: String) : MonsterCreateEvent
    
    data class AddDamageResistance(val value: String) : MonsterCreateEvent
    data class RemoveDamageResistance(val value: String) : MonsterCreateEvent
    
    data class AddDamageVulnerability(val value: String) : MonsterCreateEvent
    data class RemoveDamageVulnerability(val value: String) : MonsterCreateEvent
    
    data class ImageUrlChanged(val value: String) : MonsterCreateEvent
    data class AiSizeChanged(val width: Int, val height: Int) : MonsterCreateEvent
    data class AiPromptChanged(val value: String) : MonsterCreateEvent
    
    data object GenerateImage : MonsterCreateEvent
    data object SaveMonster : MonsterCreateEvent
}
