package com.dnd.helper.data.remote

import com.dnd.helper.data.remote.dto.character.*
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.data.remote.dto.equipment.*
import com.dnd.helper.data.remote.dto.game.ConditionDto
import com.dnd.helper.data.remote.dto.game.DamageTypeDto
import com.dnd.helper.data.remote.dto.game.RuleDto
import com.dnd.helper.data.remote.dto.game.RuleSectionDto
import com.dnd.helper.data.remote.dto.monster.MonsterDto
import com.dnd.helper.data.remote.dto.spell.MagicSchoolDto
import com.dnd.helper.data.remote.dto.spell.SpellDto
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

/**
 * Data source for the official D&D 5e SRD API (https://www.dnd5eapi.co/api/2014).
 *
 * - All methods return [Result] matching the project-wide error handling pattern.
 * - Responses are cached in-memory after the first successful fetch (TTL = process lifetime).
 *   SRD data never changes, so indefinite caching is safe for reference resources.
 * - The [httpClient] is the same Koin-managed client used by [KtorRemoteDataSource].
 *   It already has ContentNegotiation + JSON configured with ignoreUnknownKeys = true.
 */
class DndApiDataSource(private val httpClient: HttpClient) {

    companion object {
        private const val BASE = "https://www.dnd5eapi.co/api/2014"
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

    // ── Character Data ────────────────────────────────────────────────────────

    suspend fun getAbilityScores(): Result<ApiReferenceListDto> =
        cachedList("ability-scores")

    suspend fun getAbilityScore(index: String): Result<AbilityScoreDto> =
        cached(abilityScoreCache, index) { get("ability-scores/$index") }

    suspend fun getAlignments(): Result<ApiReferenceListDto> =
        cachedList("alignments")

    suspend fun getAlignment(index: String): Result<AlignmentDto> =
        cached(alignmentCache, index) { get("alignments/$index") }

    suspend fun getBackgrounds(): Result<ApiReferenceListDto> =
        cachedList("backgrounds")

    suspend fun getBackground(index: String): Result<BackgroundDto> =
        cached(backgroundCache, index) { get("backgrounds/$index") }

    suspend fun getClasses(): Result<ApiReferenceListDto> =
        cachedList("classes")

    suspend fun getClass(index: String): Result<ClassDto> =
        cached(classCache, index) { get("classes/$index") }

    /**
     * Returns all 20 class levels for a given class (e.g. "wizard").
     * Results are cached per class index.
     */
    suspend fun getClassLevels(classIndex: String): Result<List<ClassLevelDto>> =
        cached(classLevelCache, classIndex) { get("classes/$classIndex/levels") }

    suspend fun getRaces(): Result<ApiReferenceListDto> =
        cachedList("races")

    suspend fun getRace(index: String): Result<RaceDto> =
        cached(raceCache, index) { get("races/$index") }

    suspend fun getSubraces(): Result<ApiReferenceListDto> =
        cachedList("subraces")

    suspend fun getSubrace(index: String): Result<SubraceDto> =
        cached(subraceCache, index) { get("subraces/$index") }

    suspend fun getSubclasses(): Result<ApiReferenceListDto> =
        cachedList("subclasses")

    suspend fun getSubclass(index: String): Result<SubclassDto> =
        cached(subclassCache, index) { get("subclasses/$index") }

    suspend fun getTraits(): Result<ApiReferenceListDto> =
        cachedList("traits")

    suspend fun getTrait(index: String): Result<TraitDto> =
        cached(traitCache, index) { get("traits/$index") }

    suspend fun getFeatures(): Result<ApiReferenceListDto> =
        cachedList("features")

    suspend fun getFeature(index: String): Result<FeatureDto> =
        cached(featureCache, index) { get("features/$index") }

    suspend fun getFeats(): Result<ApiReferenceListDto> =
        cachedList("feats")

    suspend fun getFeat(index: String): Result<FeatDto> =
        cached(featCache, index) { get("feats/$index") }

    suspend fun getSkills(): Result<ApiReferenceListDto> =
        cachedList("skills")

    suspend fun getSkill(index: String): Result<DndSkillDto> =
        cached(skillApiCache, index) { get("skills/$index") }

    suspend fun getProficiencies(): Result<ApiReferenceListDto> =
        cachedList("proficiencies")

    suspend fun getProficiency(index: String): Result<ProficiencyDto> =
        cached(proficiencyCache, index) { get("proficiencies/$index") }

    suspend fun getLanguages(): Result<ApiReferenceListDto> =
        cachedList("languages")

    suspend fun getLanguage(index: String): Result<LanguageDto> =
        cached(languageCache, index) { get("languages/$index") }

    // ── Spells ────────────────────────────────────────────────────────────────

    /**
     * Returns the list of spells, optionally filtered.
     *
     * @param levels  List of spell levels to include (e.g. [0] for cantrips, [1, 2] for L1+L2).
     * @param school  Magic school index (e.g. "evocation"). Null = all schools.
     * @param classes Class index (e.g. "wizard"). Null = all classes.
     *
     * Note: filtered lists are NOT cached because the params vary.
     */
    suspend fun getSpells(
        levels: List<Int>? = null,
        school: String? = null,
        classes: String? = null,
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

    suspend fun getSpell(index: String): Result<SpellDto> =
        cached(spellCache, index) { get("spells/$index") }

    suspend fun getMagicSchools(): Result<ApiReferenceListDto> =
        cachedList("magic-schools")

    suspend fun getMagicSchool(index: String): Result<MagicSchoolDto> =
        cached(schoolCache, index) { get("magic-schools/$index") }

    // ── Equipment ─────────────────────────────────────────────────────────────

    suspend fun getEquipment(): Result<ApiReferenceListDto> =
        cachedList("equipment")

    /**
     * Fetches a piece of equipment as a [WeaponDto].
     * Use this for items in the "Weapon" equipment category.
     */
    suspend fun getWeapon(index: String): Result<WeaponDto> =
        cached(weaponDtoCache, index) { get("equipment/$index") }

    /**
     * Fetches a piece of equipment as an [ArmorDto].
     * Use this for items in the "Armor" equipment category.
     */
    suspend fun getArmor(index: String): Result<ArmorDto> =
        cached(armorCache, index) { get("equipment/$index") }

    /**
     * Fetches a piece of equipment as a [GearDto].
     * Use this for adventuring gear.
     */
    suspend fun getGear(index: String): Result<GearDto> =
        cached(gearCache, index) { get("equipment/$index") }

    /**
     * Fetches a piece of equipment as an [EquipmentPackDto].
     * Use this for packs (e.g. "explorer's pack").
     */
    suspend fun getEquipmentPack(index: String): Result<EquipmentPackDto> =
        cached(packCache, index) { get("equipment/$index") }

    suspend fun getEquipmentCategories(): Result<ApiReferenceListDto> =
        cachedList("equipment-categories")

    suspend fun getEquipmentCategory(index: String): Result<EquipmentCategoryDto> =
        cached(equipCategoryCache, index) { get("equipment-categories/$index") }

    suspend fun getMagicItems(): Result<ApiReferenceListDto> =
        cachedList("magic-items")

    suspend fun getMagicItem(index: String): Result<MagicItemDto> =
        cached(magicItemCache, index) { get("magic-items/$index") }

    suspend fun getWeaponProperties(): Result<ApiReferenceListDto> =
        cachedList("weapon-properties")

    suspend fun getWeaponProperty(index: String): Result<WeaponPropertyDto> =
        cached(weaponPropertyCache, index) { get("weapon-properties/$index") }

    // ── Monsters ──────────────────────────────────────────────────────────────

    /**
     * Returns the monster list, optionally filtered by challenge rating(s).
     *
     * @param challengeRatings List of CR values to include (e.g. [0.25, 0.5, 1.0]).
     *                         Null = all monsters.
     */
    suspend fun getMonsters(challengeRatings: List<Double>? = null): Result<ApiReferenceListDto> {
        if (challengeRatings == null) return cachedList("monsters")
        return safeApiCall {
            httpClient.get("$BASE/monsters") {
                challengeRatings.forEach { parameter("challenge_rating", it) }
            }
        }
    }

    suspend fun getMonster(index: String): Result<MonsterDto> =
        cached(monsterCache, index) { get("monsters/$index") }

    // ── Game Mechanics ────────────────────────────────────────────────────────

    suspend fun getConditions(): Result<ApiReferenceListDto> =
        cachedList("conditions")

    suspend fun getCondition(index: String): Result<ConditionDto> =
        cached(conditionCache, index) { get("conditions/$index") }

    suspend fun getDamageTypes(): Result<ApiReferenceListDto> =
        cachedList("damage-types")

    suspend fun getDamageType(index: String): Result<DamageTypeDto> =
        cached(damageTypeCache, index) { get("damage-types/$index") }

    // ── Rules ─────────────────────────────────────────────────────────────────

    suspend fun getRules(): Result<ApiReferenceListDto> =
        cachedList("rules")

    suspend fun getRule(index: String): Result<RuleDto> =
        cached(ruleCache, index) { get("rules/$index") }

    suspend fun getRuleSections(): Result<ApiReferenceListDto> =
        cachedList("rule-sections")

    suspend fun getRuleSection(index: String): Result<RuleSectionDto> =
        cached(ruleSectionCache, index) { get("rule-sections/$index") }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Cached fetch of a reference list (count + results). */
    private suspend fun cachedList(endpoint: String): Result<ApiReferenceListDto> {
        listCache[endpoint]?.let { return Result.Success(it) }
        return safeApiCall<ApiReferenceListDto> { httpClient.get("$BASE/$endpoint") }
            .also { if (it is Result.Success) listCache[endpoint] = it.data }
    }

    /**
     * Generic cached single-item fetch.
     * [block] receives a helper lambda to build the URL path relative to [BASE].
     */
    private suspend inline fun <reified T> cached(
        cache: MutableMap<String, T>,
        key: String,
        crossinline fetch: suspend HttpClient.() -> io.ktor.client.statement.HttpResponse,
    ): Result<T> {
        cache[key]?.let { return Result.Success(it) }
        return safeApiCall<T> { httpClient.fetch() }
            .also { if (it is Result.Success) cache[key] = it.data }
    }

    /** Convenience wrapper to build a full URL. */
    private suspend fun HttpClient.get(path: String) =
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
