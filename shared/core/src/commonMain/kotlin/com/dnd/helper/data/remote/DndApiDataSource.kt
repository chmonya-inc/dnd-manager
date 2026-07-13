package com.dnd.helper.data.remote

import com.dnd.helper.data.remote.DndApiDataSource.Companion.BASE
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
import com.dnd.helper.domain.storage.CharacterStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

interface DndApiDataSource {
    fun clearCache()

    suspend fun getAbilityScores(): Result<ApiReferenceListDto>
    suspend fun getAbilityScore(index: String): Result<AbilityScoreDto>
    suspend fun getAlignments(): Result<ApiReferenceListDto>
    suspend fun getAlignment(index: String): Result<AlignmentDto>
    suspend fun getBackgrounds(): Result<ApiReferenceListDto>
    suspend fun getBackground(index: String): Result<BackgroundDto>
    suspend fun getClasses(): Result<ApiReferenceListDto>
    suspend fun getClass(index: String): Result<ClassDto>
    suspend fun getClassLevels(classIndex: String): Result<List<ClassLevelDto>>
    suspend fun getRaces(): Result<ApiReferenceListDto>
    suspend fun getRace(index: String): Result<RaceDto>
    suspend fun getSubraces(): Result<ApiReferenceListDto>
    suspend fun getSubrace(index: String): Result<SubraceDto>
    suspend fun getSubclasses(): Result<ApiReferenceListDto>
    suspend fun getSubclass(index: String): Result<SubclassDto>
    suspend fun getTraits(): Result<ApiReferenceListDto>
    suspend fun getTrait(index: String): Result<TraitDto>
    suspend fun getFeatures(): Result<ApiReferenceListDto>
    suspend fun getFeature(index: String): Result<FeatureDto>
    suspend fun getFeats(): Result<ApiReferenceListDto>
    suspend fun getFeat(index: String): Result<FeatDto>
    suspend fun getSkills(): Result<ApiReferenceListDto>
    suspend fun getSkill(index: String): Result<DndSkillDto>
    suspend fun getProficiencies(): Result<ApiReferenceListDto>
    suspend fun getProficiency(index: String): Result<ProficiencyDto>
    suspend fun getLanguages(): Result<ApiReferenceListDto>
    suspend fun getLanguage(index: String): Result<LanguageDto>

    suspend fun getSpells(
        levels: List<Int>? = null,
        school: String? = null,
        classes: String? = null,
    ): Result<ApiReferenceListDto>
    suspend fun getSpell(index: String): Result<SpellDto>
    suspend fun getMagicSchools(): Result<ApiReferenceListDto>
    suspend fun getMagicSchool(index: String): Result<MagicSchoolDto>

    suspend fun getEquipment(): Result<ApiReferenceListDto>
    suspend fun getWeapon(index: String): Result<WeaponDto>
    suspend fun getArmor(index: String): Result<ArmorDto>
    suspend fun getGear(index: String): Result<GearDto>
    suspend fun getEquipmentPack(index: String): Result<EquipmentPackDto>
    suspend fun getEquipmentCategories(): Result<ApiReferenceListDto>
    suspend fun getEquipmentCategory(index: String): Result<EquipmentCategoryDto>
    suspend fun getMagicItems(): Result<ApiReferenceListDto>
    suspend fun getMagicItem(index: String): Result<MagicItemDto>
    suspend fun getWeaponProperties(): Result<ApiReferenceListDto>
    suspend fun getWeaponProperty(index: String): Result<WeaponPropertyDto>

    suspend fun getMonsters(challengeRatings: List<Double>? = null): Result<ApiReferenceListDto>
    suspend fun getMonster(index: String): Result<MonsterDto>

    suspend fun getConditions(): Result<ApiReferenceListDto>
    suspend fun getCondition(index: String): Result<ConditionDto>
    suspend fun getDamageTypes(): Result<ApiReferenceListDto>
    suspend fun getDamageType(index: String): Result<DamageTypeDto>

    suspend fun getRules(): Result<ApiReferenceListDto>
    suspend fun getRule(index: String): Result<RuleDto>
    suspend fun getRuleSections(): Result<ApiReferenceListDto>
    suspend fun getRuleSection(index: String): Result<RuleSectionDto>

    companion object {
        const val BASE = "https://www.dnd5eapi.co/api/2014"
    }
}

class KtorDndApiDataSource(
    private val httpClient: HttpClient,
    private val storage: CharacterStorage
) : DndApiDataSource {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── In-memory cache ───────────────────────────────────────────────────────

    private val listCache = mutableMapOf<String, ApiReferenceListDto>()
    private val abilityScoreCache = mutableMapOf<String, AbilityScoreDto>()
    private val alignmentCache = mutableMapOf<String, AlignmentDto>()
    private val backgroundCache = mutableMapOf<String, BackgroundDto>()
    private val classCache = mutableMapOf<String, ClassDto>()
    private val classLevelCache = mutableMapOf<String, List<ClassLevelDto>>()
    private val raceCache = mutableMapOf<String, RaceDto>()
    private val subraceCache = mutableMapOf<String, SubraceDto>()
    private val subclassCache = mutableMapOf<String, SubclassDto>()
    private val traitCache = mutableMapOf<String, TraitDto>()
    private val featureCache = mutableMapOf<String, FeatureDto>()
    private val featCache = mutableMapOf<String, FeatDto>()
    private val skillApiCache = mutableMapOf<String, DndSkillDto>()
    private val proficiencyCache = mutableMapOf<String, ProficiencyDto>()
    private val languageCache = mutableMapOf<String, LanguageDto>()
    private val spellCache = mutableMapOf<String, SpellDto>()
    private val schoolCache = mutableMapOf<String, MagicSchoolDto>()
    private val weaponDtoCache = mutableMapOf<String, WeaponDto>()
    private val armorCache = mutableMapOf<String, ArmorDto>()
    private val gearCache = mutableMapOf<String, GearDto>()
    private val packCache = mutableMapOf<String, EquipmentPackDto>()
    private val equipCategoryCache = mutableMapOf<String, EquipmentCategoryDto>()
    private val magicItemCache = mutableMapOf<String, MagicItemDto>()
    private val weaponPropertyCache = mutableMapOf<String, WeaponPropertyDto>()
    private val monsterCache = mutableMapOf<String, MonsterDto>()
    private val conditionCache = mutableMapOf<String, ConditionDto>()
    private val damageTypeCache = mutableMapOf<String, DamageTypeDto>()
    private val ruleCache = mutableMapOf<String, RuleDto>()
    private val ruleSectionCache = mutableMapOf<String, RuleSectionDto>()

    override fun clearCache() {
        listCache.clear()
        abilityScoreCache.clear()
        alignmentCache.clear()
        backgroundCache.clear()
        classCache.clear()
        classLevelCache.clear()
        raceCache.clear()
        subraceCache.clear()
        subclassCache.clear()
        traitCache.clear()
        featureCache.clear()
        featCache.clear()
        skillApiCache.clear()
        proficiencyCache.clear()
        languageCache.clear()
        spellCache.clear()
        schoolCache.clear()
        weaponDtoCache.clear()
        armorCache.clear()
        gearCache.clear()
        packCache.clear()
        equipCategoryCache.clear()
        magicItemCache.clear()
        weaponPropertyCache.clear()
        monsterCache.clear()
        conditionCache.clear()
        damageTypeCache.clear()
        ruleCache.clear()
        ruleSectionCache.clear()
        storage.clearApiCache()
    }

    // ── Character Data ────────────────────────────────────────────────────────

    override suspend fun getAbilityScores(): Result<ApiReferenceListDto> =
        cachedList("ability-scores")

    override suspend fun getAbilityScore(index: String): Result<AbilityScoreDto> =
        cached(abilityScoreCache, index) { getSrd("ability-scores/$index") }

    override suspend fun getAlignments(): Result<ApiReferenceListDto> =
        cachedList("alignments")

    override suspend fun getAlignment(index: String): Result<AlignmentDto> =
        cached(alignmentCache, index) { getSrd("alignments/$index") }

    override suspend fun getBackgrounds(): Result<ApiReferenceListDto> =
        cachedList("backgrounds")

    override suspend fun getBackground(index: String): Result<BackgroundDto> =
        cached(backgroundCache, index) { getSrd("backgrounds/$index") }

    override suspend fun getClasses(): Result<ApiReferenceListDto> =
        cachedList("classes")

    override suspend fun getClass(index: String): Result<ClassDto> =
        cached(classCache, index) { getSrd("classes/$index") }

    override suspend fun getClassLevels(classIndex: String): Result<List<ClassLevelDto>> =
        cached(classLevelCache, classIndex) { getSrd("classes/$classIndex/levels") }

    override suspend fun getRaces(): Result<ApiReferenceListDto> =
        cachedList("races")

    override suspend fun getRace(index: String): Result<RaceDto> =
        cached(raceCache, index) { getSrd("races/$index") }

    override suspend fun getSubraces(): Result<ApiReferenceListDto> =
        cachedList("subraces")

    override suspend fun getSubrace(index: String): Result<SubraceDto> =
        cached(subraceCache, index) { getSrd("subraces/$index") }

    override suspend fun getSubclasses(): Result<ApiReferenceListDto> =
        cachedList("subclasses")

    override suspend fun getSubclass(index: String): Result<SubclassDto> =
        cached(subclassCache, index) { getSrd("subclasses/$index") }

    override suspend fun getTraits(): Result<ApiReferenceListDto> =
        cachedList("traits")

    override suspend fun getTrait(index: String): Result<TraitDto> =
        cached(traitCache, index) { getSrd("traits/$index") }

    override suspend fun getFeatures(): Result<ApiReferenceListDto> =
        cachedList("features")

    override suspend fun getFeature(index: String): Result<FeatureDto> =
        cached(featureCache, index) { getSrd("features/$index") }

    override suspend fun getFeats(): Result<ApiReferenceListDto> =
        cachedList("feats")

    override suspend fun getFeat(index: String): Result<FeatDto> =
        cached(featCache, index) { getSrd("feats/$index") }

    override suspend fun getSkills(): Result<ApiReferenceListDto> =
        cachedList("skills")

    override suspend fun getSkill(index: String): Result<DndSkillDto> =
        cached(skillApiCache, index) { getSrd("skills/$index") }

    override suspend fun getProficiencies(): Result<ApiReferenceListDto> =
        cachedList("proficiencies")

    override suspend fun getProficiency(index: String): Result<ProficiencyDto> =
        cached(proficiencyCache, index) { getSrd("proficiencies/$index") }

    override suspend fun getLanguages(): Result<ApiReferenceListDto> =
        cachedList("languages")

    override suspend fun getLanguage(index: String): Result<LanguageDto> =
        cached(languageCache, index) { getSrd("languages/$index") }

    // ── Spells ────────────────────────────────────────────────────────────────

    override suspend fun getSpells(
        levels: List<Int>?,
        school: String?,
        classes: String?,
    ): Result<ApiReferenceListDto> {
        // Only cache unfiltered full list
        if (levels == null && school == null && classes == null) {
            return cachedList("spells")
        }
        return safeApiCall {
            httpClient.get("$BASE/spells") {
                levels?.forEach { parameter("level", it) }
                school?.let { parameter("school", it) }
                classes?.let { parameter("classes", it) }
            }
        }
    }

    override suspend fun getSpell(index: String): Result<SpellDto> =
        cached(spellCache, index) { getSrd("spells/$index") }

    override suspend fun getMagicSchools(): Result<ApiReferenceListDto> =
        cachedList("magic-schools")

    override suspend fun getMagicSchool(index: String): Result<MagicSchoolDto> =
        cached(schoolCache, index) { getSrd("magic-schools/$index") }

    // ── Equipment ─────────────────────────────────────────────────────────────

    override suspend fun getEquipment(): Result<ApiReferenceListDto> =
        cachedList("equipment")

    override suspend fun getWeapon(index: String): Result<WeaponDto> =
        cached(weaponDtoCache, index) { getSrd("equipment/$index") }

    override suspend fun getArmor(index: String): Result<ArmorDto> =
        cached(armorCache, index) { getSrd("equipment/$index") }

    override suspend fun getGear(index: String): Result<GearDto> =
        cached(gearCache, index) { getSrd("equipment/$index") }

    override suspend fun getEquipmentPack(index: String): Result<EquipmentPackDto> =
        cached(packCache, index) { getSrd("equipment/$index") }

    override suspend fun getEquipmentCategories(): Result<ApiReferenceListDto> =
        cachedList("equipment-categories")

    override suspend fun getEquipmentCategory(index: String): Result<EquipmentCategoryDto> =
        cached(equipCategoryCache, index) { getSrd("equipment-categories/$index") }

    override suspend fun getMagicItems(): Result<ApiReferenceListDto> =
        cachedList("magic-items")

    override suspend fun getMagicItem(index: String): Result<MagicItemDto> =
        cached(magicItemCache, index) { getSrd("magic-items/$index") }

    override suspend fun getWeaponProperties(): Result<ApiReferenceListDto> =
        cachedList("weapon-properties")

    override suspend fun getWeaponProperty(index: String): Result<WeaponPropertyDto> =
        cached(weaponPropertyCache, index) { getSrd("weapon-properties/$index") }

    // ── Monsters ──────────────────────────────────────────────────────────────

    override suspend fun getMonsters(challengeRatings: List<Double>?): Result<ApiReferenceListDto> {
        if (challengeRatings == null) return cachedList("monsters")
        return safeApiCall {
            httpClient.get("$BASE/monsters") {
                challengeRatings.forEach { parameter("challenge_rating", it) }
            }
        }
    }

    override suspend fun getMonster(index: String): Result<MonsterDto> =
        cached(monsterCache, index) { getSrd("monsters/$index") }

    // ── Game Mechanics ────────────────────────────────────────────────────────

    override suspend fun getConditions(): Result<ApiReferenceListDto> =
        cachedList("conditions")

    override suspend fun getCondition(index: String): Result<ConditionDto> =
        cached(conditionCache, index) { getSrd("conditions/$index") }

    override suspend fun getDamageTypes(): Result<ApiReferenceListDto> =
        cachedList("damage-types")

    override suspend fun getDamageType(index: String): Result<DamageTypeDto> =
        cached(damageTypeCache, index) { getSrd("damage-types/$index") }

    // ── Rules ─────────────────────────────────────────────────────────────────

    override suspend fun getRules(): Result<ApiReferenceListDto> =
        cachedList("rules")

    override suspend fun getRule(index: String): Result<RuleDto> =
        cached(ruleCache, index) { getSrd("rules/$index") }

    override suspend fun getRuleSections(): Result<ApiReferenceListDto> =
        cachedList("rule-sections")

    override suspend fun getRuleSection(index: String): Result<RuleSectionDto> =
        cached(ruleSectionCache, index) { getSrd("rule-sections/$index") }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Cached fetch of a reference list (count + results). */
    private suspend fun cachedList(endpoint: String): Result<ApiReferenceListDto> {
        val cacheKey = "list_${endpoint.replace("/", "_")}"
        listCache[endpoint]?.let { return Result.Success(it) }

        // Try persistent cache
        storage.getApiCache(cacheKey)?.let { jsonStr ->
            try {
                val decoded = jsonParser.decodeFromString<ApiReferenceListDto>(jsonStr)
                listCache[endpoint] = decoded
                return Result.Success(decoded)
            } catch (e: Exception) {
                // Parse error, ignore and fetch fresh
            }
        }

        return safeApiCall<ApiReferenceListDto> { httpClient.getSrd(endpoint) }
            .also { result ->
                if (result is Result.Success) {
                    listCache[endpoint] = result.data
                    storage.saveApiCache(cacheKey, jsonParser.encodeToString(result.data))
                }
            }
    }

    /**
     * Generic cached single-item fetch.
     * [fetch] receives a helper lambda to build the URL path relative to [BASE].
     */
    private suspend inline fun <reified T> cached(
        cache: MutableMap<String, T>,
        key: String,
        crossinline fetch: suspend HttpClient.() -> io.ktor.client.statement.HttpResponse,
    ): Result<T> {
        val cacheKey = "item_${T::class.simpleName}_${key.replace("/", "_")}"
        cache[key]?.let { return Result.Success(it) }

        // Try persistent cache
        storage.getApiCache(cacheKey)?.let { jsonStr ->
            try {
                val decoded = jsonParser.decodeFromString<T>(jsonStr)
                cache[key] = decoded
                return Result.Success(decoded)
            } catch (e: Exception) {
                // Parse error, ignore and fetch fresh
            }
        }

        return safeApiCall<T> { httpClient.fetch() }
            .also { result ->
                if (result is Result.Success) {
                    cache[key] = result.data
                    storage.saveApiCache(cacheKey, jsonParser.encodeToString(result.data))
                }
            }
    }

    /** Convenience wrapper to build a full URL for SRD resources. */
    private suspend fun HttpClient.getSrd(path: String): io.ktor.client.statement.HttpResponse =
        this.get("$BASE/$path")

    /** Mirrors the [safeApiCall] pattern from [KtorRemoteDataSource]. */
    private suspend inline fun <reified T> safeApiCall(
        call: () -> io.ktor.client.statement.HttpResponse,
    ): Result<T> {
        return try {
            val response = call()
            if (response.status.isSuccess()) {
                if (T::class == Unit::class) {
                    @Suppress("UNCHECKED_CAST")
                    Result.Success(Unit as T)
                } else {
                    Result.Success(response.body<T>())
                }
            } else {
                Result.Error(AppError.Unknown("DnD API returned ${response.status}"))
            }
        } catch (e: Exception) {
            println("[DndApiDataSource] Error: ${e.message}")
            Result.Error(AppError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
