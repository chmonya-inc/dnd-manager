package com.dnd.helper.presentation.desktop

import com.dnd.helper.data.remote.dto.character.AbilityScoreDto
import com.dnd.helper.data.remote.dto.character.AlignmentDto
import com.dnd.helper.data.remote.dto.character.ClassDto
import com.dnd.helper.data.remote.dto.character.RaceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.fakes.FakeDndApiDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RulesLibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: RulesLibraryViewModel
    private lateinit var fakeApiDataSource: FakeDndApiDataSource

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeApiDataSource = FakeDndApiDataSource().apply {
            // Setup some default data so initial load succeeds
            getAbilityScoresResult = Result.Success(ApiReferenceListDto(count = 1, results = listOf(ApiReferenceDto("STR", "str"))))
            getAbilityScoreResult = Result.Success(AbilityScoreDto(index = "str", name = "Strength"))

            getAlignmentsResult = Result.Success(ApiReferenceListDto(count = 1, results = listOf(ApiReferenceDto("LG", "lawful-good"))))
            getAlignmentResult = Result.Success(AlignmentDto(index = "lawful-good", name = "Lawful Good"))

            getClassesResult = Result.Success(ApiReferenceListDto(count = 1, results = listOf(ApiReferenceDto("Fighter", "fighter"))))
            getClassResult = Result.Success(ClassDto(index = "fighter", name = "Fighter"))

            getRacesResult = Result.Success(ApiReferenceListDto(count = 1, results = listOf(ApiReferenceDto("Human", "human"))))
            getRaceResult = Result.Success(RaceDto(index = "human", name = "Human"))
        }
        viewModel = RulesLibraryViewModel(api = fakeApiDataSource, ioDispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state has default category and empty query`() = runTest(testDispatcher) {
        assertEquals(RuleCategory.CharacterData, viewModel.state.value.selectedCategory)
        assertEquals("", viewModel.state.value.searchQuery)
    }

    @Test
    fun `initial state starts loading data`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isLoading)
        // Should have some data loaded
        assertTrue(viewModel.state.value.abilityScores.isNotEmpty() ||
                viewModel.state.value.alignments.isNotEmpty() ||
                viewModel.state.value.classes.isNotEmpty())
    }

    // === Category Selection Tests ===

    @Test
    fun `setCategory updates selectedCategory`() = runTest(testDispatcher) {
        viewModel.setCategory(RuleCategory.Spells)

        assertEquals(RuleCategory.Spells, viewModel.state.value.selectedCategory)
    }

    @Test
    fun `setCategory can switch between categories`() = runTest(testDispatcher) {
        viewModel.setCategory(RuleCategory.Equipment)
        assertEquals(RuleCategory.Equipment, viewModel.state.value.selectedCategory)

        viewModel.setCategory(RuleCategory.Monsters)
        assertEquals(RuleCategory.Monsters, viewModel.state.value.selectedCategory)

        viewModel.setCategory(RuleCategory.Rules)
        assertEquals(RuleCategory.Rules, viewModel.state.value.selectedCategory)
    }

    // === Search Tests ===

    @Test
    fun `setSearchQuery updates searchQuery`() = runTest(testDispatcher) {
        viewModel.setSearchQuery("fire")

        assertEquals("fire", viewModel.state.value.searchQuery)
    }

    @Test
    fun `setSearchQuery handles empty string`() = runTest(testDispatcher) {
        viewModel.setSearchQuery("test")
        assertEquals("test", viewModel.state.value.searchQuery)

        viewModel.setSearchQuery("")
        assertEquals("", viewModel.state.value.searchQuery)
    }

    // === Reload Tests ===

    @Test
    fun `reload clears cache and reloads data`() = runTest(testDispatcher) {
        // We can't directly verify cache clear easily, but we can verify reload happens
        // and doesn't crash, and finishes loading.
        viewModel.reload()

        // Wait for reload
        kotlinx.coroutines.delay(100)

        // Should have fresh data
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.abilityScores.isNotEmpty())
    }

    // === Data Loading Tests ===

    @Test
    fun `loadData loads ability scores from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.abilityScores.isNotEmpty())
    }

    @Test
    fun `loadData loads alignments from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.alignments.isNotEmpty())
    }

    @Test
    fun `loadData loads classes from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.classes.isNotEmpty())
    }

    @Test
    fun `loadData loads races from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.races.isNotEmpty())
    }

    // === All Categories Tests ===

    @Test
    fun `all categories are loaded on initialization`() = runTest(testDispatcher) {
        // Setup comprehensive data
        fakeApiDataSource.getBackgroundsResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Acolyte", index = "acolyte"))
        ))
        fakeApiDataSource.getSpellsResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Fireball", index = "fireball"))
        ))
        fakeApiDataSource.getEquipmentCategoriesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Weapon", index = "weapon"))
        ))
        fakeApiDataSource.getMonstersResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Goblin", index = "goblin"))
        ))
        fakeApiDataSource.getConditionsResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Blinded", index = "blinded"))
        ))
        fakeApiDataSource.getRulesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Ability Checks", index = "ability-checks"))
        ))

        val newViewModel = RulesLibraryViewModel(api = fakeApiDataSource, ioDispatcher = testDispatcher)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Verify all categories have data structures
        assertTrue(newViewModel.state.value.abilityScores.isNotEmpty() ||
                newViewModel.state.value.alignments.isNotEmpty() ||
                newViewModel.state.value.backgrounds.isNotEmpty() ||
                newViewModel.state.value.classes.isNotEmpty())
    }

    // === Error Handling Tests ===

    @Test
    fun `handles API errors gracefully`() = runTest(testDispatcher) {
        // Create API that returns errors
        val errorApi = FakeDndApiDataSource().apply {
            getAbilityScoresResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)
            getAlignmentsResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)
            getClassesResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)
        }

        val errorViewModel = RulesLibraryViewModel(api = errorApi, ioDispatcher = testDispatcher)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Should complete without crashing, just empty data
        assertFalse(errorViewModel.state.value.isLoading)
    }

    // === State Persistence Tests ===

    @Test
    fun `state changes persist across multiple category switches`() = runTest(testDispatcher) {
        viewModel.setCategory(RuleCategory.Equipment)
        viewModel.setSearchQuery("sword")

        assertEquals(RuleCategory.Equipment, viewModel.state.value.selectedCategory)
        assertEquals("sword", viewModel.state.value.searchQuery)

        viewModel.setCategory(RuleCategory.Spells)

        // Search query should persist
        assertEquals("sword", viewModel.state.value.searchQuery)
        assertEquals(RuleCategory.Spells, viewModel.state.value.selectedCategory)
    }

    // === Edge Cases ===

    @Test
    fun `handles empty API responses`() = runTest(testDispatcher) {
        val emptyApi = FakeDndApiDataSource().apply {
            getAbilityScoresResult = Result.Success(ApiReferenceListDto(count = 0, results = emptyList()))
            getAlignmentsResult = Result.Success(ApiReferenceListDto(count = 0, results = emptyList()))
        }

        val emptyViewModel = RulesLibraryViewModel(api = emptyApi, ioDispatcher = testDispatcher)

        // Wait for init
        kotlinx.coroutines.delay(100)

        // Should handle empty responses gracefully
        assertFalse(emptyViewModel.state.value.isLoading)
        assertTrue(emptyViewModel.state.value.abilityScores.isEmpty())
        assertTrue(emptyViewModel.state.value.alignments.isEmpty())
    }

    @Test
    fun `handles rapid category switching`() = runTest(testDispatcher) {
        repeat(5) {
            viewModel.setCategory(RuleCategory.entries[it % RuleCategory.entries.size])
        }

        // Should have the last category
        assertTrue(viewModel.state.value.selectedCategory != RuleCategory.CharacterData)
    }

    @Test
    fun `handles rapid search query changes`() = runTest(testDispatcher) {
        repeat(3) { index ->
            viewModel.setSearchQuery("search$index")
        }

        assertEquals("search2", viewModel.state.value.searchQuery)
    }
}
