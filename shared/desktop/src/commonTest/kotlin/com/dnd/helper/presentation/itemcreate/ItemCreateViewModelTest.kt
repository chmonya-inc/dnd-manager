package com.dnd.helper.presentation.itemcreate

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ApiReferenceListDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.fakes.FakeCharacterRepository
import com.dnd.helper.fakes.FakeDndApiDataSource
import com.dnd.helper.fakes.FakeEditingRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ItemCreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ItemCreateViewModel
    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeEditingRepository: FakeEditingRepository
    private lateinit var fakeApiDataSource: FakeDndApiDataSource

    private fun createTestCharacter(id: String = "char-1") = Character(
        id = id,
        name = "Test Character",
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 1,
        description = "Test",
        maxHp = 20,
        currentHp = 20,
        items = emptyList()
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()
        fakeEditingRepository = FakeEditingRepository()
        fakeApiDataSource = FakeDndApiDataSource()

        // Setup some default API data
        fakeApiDataSource.getWeaponPropertiesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Finesse", index = "finesse"))
        ))
        fakeApiDataSource.getEquipmentCategoriesResult = Result.Success(ApiReferenceListDto(
            count = 1,
            results = listOf(ApiReferenceDto(name = "Weapon", index = "weapon"))
        ))
        fakeRepository.getCharactersResult = Result.Success(listOf(createTestCharacter()))

        viewModel = ItemCreateViewModel(
            repository = fakeRepository,
            editingRepository = fakeEditingRepository,
            api = fakeApiDataSource
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state is empty`() = runTest(testDispatcher) {
        assertEquals("", viewModel.state.value.name)
        assertEquals("", viewModel.state.value.description)
        assertEquals(ItemRarity.COMMON, viewModel.state.value.rarity)
        assertEquals(EquipmentSlot.MAIN_HAND, viewModel.state.value.slot)
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isSaveSuccessful)
    }

    @Test
    fun `initData loads existing item data`() = runTest(testDispatcher) {
        val existingItem = Item(
            id = "item-1",
            name = "Existing Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.RARE,
            description = "Test item",
            weight = 2.5,
            cost = "100 gp"
        )

        viewModel.initData(existingItem, "char-1")

        assertEquals("Existing Item", viewModel.state.value.name)
        assertEquals("Test item", viewModel.state.value.description)
        assertEquals(ItemRarity.RARE, viewModel.state.value.rarity)
        assertEquals("100 gp", viewModel.state.value.cost)
    }

    @Test
    fun `initial state loads available data from API`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.availableProperties.isNotEmpty())
        assertTrue(viewModel.state.value.availableEquipmentCategories.isNotEmpty())
    }

    // === Basic Info Tests ===

    @Test
    fun `NameChanged updates name in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.NameChanged("Magic Sword"))

        assertEquals("Magic Sword", viewModel.state.value.name)
    }

    @Test
    fun `DescriptionChanged updates description in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.DescriptionChanged("A powerful magical sword"))

        assertEquals("A powerful magical sword", viewModel.state.value.description)
    }

    @Test
    fun `SlotChanged updates slot in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.SlotChanged(EquipmentSlot.OFF_HAND))

        assertEquals(EquipmentSlot.OFF_HAND, viewModel.state.value.slot)
    }

    @Test
    fun `RarityChanged updates rarity in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.RarityChanged(ItemRarity.LEGENDARY))

        assertEquals(ItemRarity.LEGENDARY, viewModel.state.value.rarity)
    }

    @Test
    fun `CostChanged updates cost in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.CostChanged("500 gp"))

        assertEquals("500 gp", viewModel.state.value.cost)
    }

    @Test
    fun `WeightChanged updates weight in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.WeightChanged("3.5"))

        assertEquals("3.5", viewModel.state.value.weight)
    }

    @Test
    fun `TypeChanged updates type in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.TypeChanged("Martial Weapon"))

        assertEquals("Martial Weapon", viewModel.state.value.type)
    }

    @Test
    fun `EquippedChanged updates isEquipped in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.EquippedChanged(true))

        assertTrue(viewModel.state.value.isEquipped)
    }

    // === Property Management Tests ===

    @Test
    fun `PropertyToggled adds property if not present`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Finesse"))

        assertTrue("Finesse" in viewModel.state.value.properties)
    }

    @Test
    fun `PropertyToggled removes property if present`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Finesse"))
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Finesse"))

        assertFalse("Finesse" in viewModel.state.value.properties)
    }

    @Test
    fun `multiple properties can be added`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Finesse"))
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Versatile"))
        viewModel.onEvent(ItemCreateEvent.PropertyToggled("Thrown"))

        assertEquals(3, viewModel.state.value.properties.size)
        assertTrue("Finesse" in viewModel.state.value.properties)
        assertTrue("Versatile" in viewModel.state.value.properties)
        assertTrue("Thrown" in viewModel.state.value.properties)
    }

    // === Owner Management Tests ===

    @Test
    fun `OwnerChanged updates characterId in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.OwnerChanged("char-123"))

        assertEquals("char-123", viewModel.state.value.characterId)
    }

    @Test
    fun `initial state loads characters from repository`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.characters.isNotEmpty())
    }

    // === Image Generation Tests ===

    @Test
    fun `GenerateImageClicked starts generation and updates imageUrl`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.NameChanged("Magic Sword"))
        viewModel.onEvent(ItemCreateEvent.GenerateImageClicked)

        // Wait for generation
        kotlinx.coroutines.delay(100)

        assertEquals("item", fakeEditingRepository.lastStartedEntityType)
        assertTrue(viewModel.state.value.imageUrl.startsWith("generating:"))
    }

    @Test
    fun `AiPromptChanged updates aiPrompt in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.AiPromptChanged("Custom AI prompt"))

        assertEquals("Custom AI prompt", viewModel.state.value.aiPrompt)
    }

    @Test
    fun `AiWidthChanged updates aiWidth in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.AiWidthChanged("800"))

        assertEquals("800", viewModel.state.value.aiWidth)
    }

    @Test
    fun `AiHeightChanged updates aiHeight in state`() = runTest(testDispatcher) {
        viewModel.onEvent(ItemCreateEvent.AiHeightChanged("600"))

        assertEquals("600", viewModel.state.value.aiHeight)
    }

    // === Save Item Tests ===

    @Test
    fun `SaveClicked saves item to character in repository`() = runTest(testDispatcher) {
        val character = createTestCharacter()
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.initData(null, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Magic Sword"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
        assertFalse(viewModel.state.value.isSaving)
        assertTrue(viewModel.state.value.isSaveSuccessful)
    }

    @Test
    fun `SaveClicked shows error on failure`() = runTest(testDispatcher) {
        val character = createTestCharacter()
        fakeRepository.saveCharacterResult = Result.Error(com.dnd.helper.domain.common.AppError.Network)

        viewModel.initData(null, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Magic Sword"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.saveError)
    }

    @Test
    fun `SaveClicked fails without character owner`() = runTest(testDispatcher) {
        viewModel.initData(null, null)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Magic Sword"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertFalse(viewModel.state.value.isSaving)
        assertNotNull(viewModel.state.value.saveError)
        assertTrue(viewModel.state.value.saveError?.contains("owner") == true)
    }

    @Test
    fun `SaveClicked updates existing item in character items`() = runTest(testDispatcher) {
        val existingItem = Item(
            id = "item-1",
            name = "Old Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "Old description",
            weight = 1.0
        )
        val character = createTestCharacter().copy(items = listOf(existingItem))
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.initData(existingItem, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Updated Item"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
        // Should update the existing item, not add a new one
        assertEquals(1, fakeRepository.savedCharacters[character.id]?.items?.size)
    }

    @Test
    fun `SaveClicked adds new item to character items`() = runTest(testDispatcher) {
        val character = createTestCharacter()
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.initData(null, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("New Item"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
        val savedCharacter = fakeRepository.savedCharacters[character.id]
        assertEquals(1, savedCharacter?.items?.size)
        assertEquals("New Item", savedCharacter?.items?.get(0)?.name)
    }

    // === Edge Cases ===

    @Test
    fun `handles rapid field updates correctly`() = runTest(testDispatcher) {
        repeat(3) { index ->
            viewModel.onEvent(ItemCreateEvent.CostChanged(index.toString()))
        }

        assertEquals("2", viewModel.state.value.cost)
    }

    @Test
    fun `SaveClicked parses weight correctly`() = runTest(testDispatcher) {
        val character = createTestCharacter()
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.initData(null, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Heavy Item"))
        viewModel.onEvent(ItemCreateEvent.WeightChanged("2.5"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        val savedCharacter = fakeRepository.savedCharacters[character.id]
        assertEquals(2.5, savedCharacter?.items?.get(0)?.weight)
    }

    @Test
    fun `SaveClicked handles invalid weight gracefully`() = runTest(testDispatcher) {
        val character = createTestCharacter()
        fakeRepository.saveCharacterResult = Result.Success(Unit)

        viewModel.initData(null, character.id)
        viewModel.onEvent(ItemCreateEvent.NameChanged("Test Item"))
        viewModel.onEvent(ItemCreateEvent.WeightChanged("invalid"))
        viewModel.onEvent(ItemCreateEvent.SaveClicked)

        // Wait for save
        kotlinx.coroutines.delay(100)

        val savedCharacter = fakeRepository.savedCharacters[character.id]
        assertEquals(0.0, savedCharacter?.items?.get(0)?.weight)
    }

    @Test
    fun `initData only initializes once`() = runTest(testDispatcher) {
        val existingItem = Item(
            id = "item-1",
            name = "First Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "First",
            weight = 1.0
        )

        viewModel.initData(existingItem, "char-1")
        assertEquals("First Item", viewModel.state.value.name)

        val secondItem = existingItem.copy(name = "Second Item")
        viewModel.initData(secondItem, "char-1")

        // Should still be first item
        assertEquals("First Item", viewModel.state.value.name)
    }
}
