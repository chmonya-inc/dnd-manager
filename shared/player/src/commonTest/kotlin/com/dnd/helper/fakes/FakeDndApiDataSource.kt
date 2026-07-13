package com.dnd.helper.fakes

import com.dnd.helper.data.remote.DndApiDataSource
import com.dnd.helper.data.remote.dto.character.AbilityScoreDto
import com.dnd.helper.data.remote.dto.character.AlignmentDto
import com.dnd.helper.data.remote.dto.character.BackgroundDto
import com.dnd.helper.data.remote.dto.character.ClassDto
import com.dnd.helper.data.remote.dto.character.ClassLevelDto
import com.dnd.helper.data.remote.dto.character.DndSkillDto
import com.dnd.helper.data.remote.dto.character.FeatDto
import com.dnd.helper.data.remote.dto.character.FeatureDto
import com.dnd.helper.data.remote.dto.character.LanguageDto
import com.dnd.helper.data.remote.dto.character.ProficiencyDto
import com.dnd.helper.data.remote.dto.character.RaceDto
import com.dnd.helper.data.remote.dto.character.SubclassDto
import com.dnd.helper.data.remote.dto.character.SubraceDto
import com.dnd.helper.data.remote.dto.character.TraitDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.data.remote.dto.equipment.ArmorDto
import com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto
import com.dnd.helper.data.remote.dto.equipment.EquipmentPackDto
import com.dnd.helper.data.remote.dto.equipment.GearDto
import com.dnd.helper.data.remote.dto.equipment.MagicItemDto
import com.dnd.helper.data.remote.dto.equipment.WeaponDto
import com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto
import com.dnd.helper.data.remote.dto.game.ConditionDto
import com.dnd.helper.data.remote.dto.game.DamageTypeDto
import com.dnd.helper.data.remote.dto.game.RuleDto
import com.dnd.helper.data.remote.dto.game.RuleSectionDto
import com.dnd.helper.data.remote.dto.monster.MonsterDto
import com.dnd.helper.data.remote.dto.spell.MagicSchoolDto
import com.dnd.helper.data.remote.dto.spell.SpellDto
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result

class FakeDndApiDataSource : DndApiDataSource {
    var getClassesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getRacesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getBackgroundsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getAlignmentsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getLanguagesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getSkillsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getProficienciesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getTraitsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getFeaturesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getFeatsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getSubracesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getSubclassesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getSpellsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getMagicSchoolsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getEquipmentResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getEquipmentCategoriesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getMagicItemsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getWeaponPropertiesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getMonstersResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getConditionsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getDamageTypesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getRulesResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getRuleSectionsResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())
    var getAbilityScoresResult: Result<ApiReferenceListDto> = Result.Success(ApiReferenceListDto())

    var getClassResult: Result<ClassDto> = Result.Error(AppError.NotFound)
    var getRaceResult: Result<RaceDto> = Result.Error(AppError.NotFound)
    var getBackgroundResult: Result<BackgroundDto> = Result.Error(AppError.NotFound)
    var getAlignmentResult: Result<AlignmentDto> = Result.Error(AppError.NotFound)
    var getLanguageResult: Result<LanguageDto> = Result.Error(AppError.NotFound)
    var getSkillResult: Result<DndSkillDto> = Result.Error(AppError.NotFound)
    var getProficiencyResult: Result<ProficiencyDto> = Result.Error(AppError.NotFound)
    var getTraitResult: Result<TraitDto> = Result.Error(AppError.NotFound)
    var getFeatureResult: Result<FeatureDto> = Result.Error(AppError.NotFound)
    var getFeatResult: Result<FeatDto> = Result.Error(AppError.NotFound)
    var getSubraceResult: Result<SubraceDto> = Result.Error(AppError.NotFound)
    var getSubclassResult: Result<SubclassDto> = Result.Error(AppError.NotFound)
    var getSpellResult: Result<SpellDto> = Result.Error(AppError.NotFound)
    var getMagicSchoolResult: Result<MagicSchoolDto> = Result.Error(AppError.NotFound)
    var getWeaponResult: Result<WeaponDto> = Result.Error(AppError.NotFound)
    var getArmorResult: Result<ArmorDto> = Result.Error(AppError.NotFound)
    var getGearResult: Result<GearDto> = Result.Error(AppError.NotFound)
    var getEquipmentPackResult: Result<EquipmentPackDto> = Result.Error(AppError.NotFound)
    var getEquipmentCategoryResult: Result<EquipmentCategoryDto> = Result.Error(AppError.NotFound)
    var getMagicItemResult: Result<MagicItemDto> = Result.Error(AppError.NotFound)
    var getWeaponPropertyResult: Result<WeaponPropertyDto> = Result.Error(AppError.NotFound)
    var getMonsterResult: Result<MonsterDto> = Result.Error(AppError.NotFound)
    var getConditionResult: Result<ConditionDto> = Result.Error(AppError.NotFound)
    var getDamageTypeResult: Result<DamageTypeDto> = Result.Error(AppError.NotFound)
    var getRuleResult: Result<RuleDto> = Result.Error(AppError.NotFound)
    var getRuleSectionResult: Result<RuleSectionDto> = Result.Error(AppError.NotFound)
    var getAbilityScoreResult: Result<AbilityScoreDto> = Result.Error(AppError.NotFound)

    override suspend fun getClasses(): Result<ApiReferenceListDto> = getClassesResult
    override suspend fun getRaces(): Result<ApiReferenceListDto> = getRacesResult
    override suspend fun getBackgrounds(): Result<ApiReferenceListDto> = getBackgroundsResult
    override suspend fun getAlignments(): Result<ApiReferenceListDto> = getAlignmentsResult
    override suspend fun getLanguages(): Result<ApiReferenceListDto> = getLanguagesResult
    override suspend fun getSkills(): Result<ApiReferenceListDto> = getSkillsResult
    override suspend fun getProficiencies(): Result<ApiReferenceListDto> = getProficienciesResult
    override suspend fun getTraits(): Result<ApiReferenceListDto> = getTraitsResult
    override suspend fun getFeatures(): Result<ApiReferenceListDto> = getFeaturesResult
    override suspend fun getFeats(): Result<ApiReferenceListDto> = getFeatsResult
    override suspend fun getSubraces(): Result<ApiReferenceListDto> = getSubracesResult
    override suspend fun getSubclasses(): Result<ApiReferenceListDto> = getSubclassesResult
    override suspend fun getSpells(levels: List<Int>?, school: String?, classes: String?): Result<ApiReferenceListDto> = getSpellsResult
    override suspend fun getMagicSchools(): Result<ApiReferenceListDto> = getMagicSchoolsResult
    override suspend fun getEquipment(): Result<ApiReferenceListDto> = getEquipmentResult
    override suspend fun getEquipmentCategories(): Result<ApiReferenceListDto> = getEquipmentCategoriesResult
    override suspend fun getMagicItems(): Result<ApiReferenceListDto> = getMagicItemsResult
    override suspend fun getWeaponProperties(): Result<ApiReferenceListDto> = getWeaponPropertiesResult
    override suspend fun getMonsters(challengeRatings: List<Double>?): Result<ApiReferenceListDto> = getMonstersResult
    override suspend fun getConditions(): Result<ApiReferenceListDto> = getConditionsResult
    override suspend fun getDamageTypes(): Result<ApiReferenceListDto> = getDamageTypesResult
    override suspend fun getRules(): Result<ApiReferenceListDto> = getRulesResult
    override suspend fun getRuleSections(): Result<ApiReferenceListDto> = getRuleSectionsResult
    override suspend fun getAbilityScores(): Result<ApiReferenceListDto> = getAbilityScoresResult

    override suspend fun getClass(index: String): Result<ClassDto> = getClassResult
    override suspend fun getRace(index: String): Result<RaceDto> = getRaceResult
    override suspend fun getBackground(index: String): Result<BackgroundDto> = getBackgroundResult
    override suspend fun getAlignment(index: String): Result<AlignmentDto> = getAlignmentResult
    override suspend fun getLanguage(index: String): Result<LanguageDto> = getLanguageResult
    override suspend fun getSkill(index: String): Result<DndSkillDto> = getSkillResult
    override suspend fun getProficiency(index: String): Result<ProficiencyDto> = getProficiencyResult
    override suspend fun getTrait(index: String): Result<TraitDto> = getTraitResult
    override suspend fun getFeature(index: String): Result<FeatureDto> = getFeatureResult
    override suspend fun getFeat(index: String): Result<FeatDto> = getFeatResult
    override suspend fun getSubrace(index: String): Result<SubraceDto> = getSubraceResult
    override suspend fun getSubclass(index: String): Result<SubclassDto> = getSubclassResult
    override suspend fun getSpell(index: String): Result<SpellDto> = getSpellResult
    override suspend fun getMagicSchool(index: String): Result<MagicSchoolDto> = getMagicSchoolResult
    override suspend fun getWeapon(index: String): Result<WeaponDto> = getWeaponResult
    override suspend fun getArmor(index: String): Result<ArmorDto> = getArmorResult
    override suspend fun getGear(index: String): Result<GearDto> = getGearResult
    override suspend fun getEquipmentPack(index: String): Result<EquipmentPackDto> = getEquipmentPackResult
    override suspend fun getEquipmentCategory(index: String): Result<EquipmentCategoryDto> = getEquipmentCategoryResult
    override suspend fun getMagicItem(index: String): Result<MagicItemDto> = getMagicItemResult
    override suspend fun getWeaponProperty(index: String): Result<WeaponPropertyDto> = getWeaponPropertyResult
    override suspend fun getMonster(index: String): Result<MonsterDto> = getMonsterResult
    override suspend fun getCondition(index: String): Result<ConditionDto> = getConditionResult
    override suspend fun getDamageType(index: String): Result<DamageTypeDto> = getDamageTypeResult
    override suspend fun getRule(index: String): Result<RuleDto> = getRuleResult
    override suspend fun getRuleSection(index: String): Result<RuleSectionDto> = getRuleSectionResult
    override suspend fun getAbilityScore(index: String): Result<AbilityScoreDto> = getAbilityScoreResult
    override suspend fun getClassLevels(classIndex: String): Result<List<ClassLevelDto>> = Result.Success(emptyList())

    override fun clearCache() {
        // No-op for fake
    }
}
