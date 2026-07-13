package com.dnd.helper.presentation.desktop

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.GameEvent
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.domain.model.PresentedItem
import com.dnd.helper.fakes.FakeCharacterRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PresentationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PresentationViewModel
    private lateinit var fakeRepository: FakeCharacterRepository

    private fun createTestMonster(id: String = "monster-1", name: String = "Goblin") = Monster(
        id = id,
        name = name,
        description = "Small goblin",
        challengeRating = "1/4",
        type = "Humanoid",
        size = "Small",
        maxHp = 10,
        currentHp = 7,
        armorClass = 15,
        stats = CharacterStats(strength = 8, dexterity = 14, constitution = 10, intelligence = 8, wisdom = 8, charisma = 6)
    )

    private fun createTestNpc(id: String = "npc-1", name: String = "Village Elder") = Npc(
        id = id,
        name = name,
        description = "Old wise man",
        background = "Retired adventurer"
    )

    private fun createTestLocation(id: String = "loc-1", name: String = "Tavern") = Location(
        id = id,
        name = name,
        description = "A cozy tavern"
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()

        // Setup test data
        fakeRepository.getMonstersResult = Result.Success(listOf(
            createTestMonster("monster-1", "Goblin"),
            createTestMonster("monster-2", "Orc")
        ))
        fakeRepository.getNpcsResult = Result.Success(listOf(createTestNpc()))
        fakeRepository.getLocationsResult = Result.Success(listOf(createTestLocation()))
        fakeRepository.getBattlefieldsResult = Result.Success(emptyList())
        fakeRepository.getEventsResult = Result.Success(emptyList<com.dnd.helper.domain.model.GameEvent>())

        viewModel = PresentationViewModel(repository = fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads monsters from repository`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.monsters.value.isNotEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `initial state loads npcs from repository`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.npcs.value.isNotEmpty())
    }

    @Test
    fun `initial state loads locations from repository`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.locations.value.isNotEmpty())
    }

    @Test
    fun `initial state has empty active items`() = runTest(testDispatcher) {
        assertTrue(viewModel.activeItems.isEmpty())
        assertNull(viewModel.activeEvent.value)
    }

    // === Window Management Tests ===

    @Test
    fun `toggleWindow changes window open state`() = runTest(testDispatcher) {
        val initialState = viewModel.isWindowOpen.value

        viewModel.toggleWindow()

        assertEquals(!initialState, viewModel.isWindowOpen.value)
    }

    @Test
    fun `setWindowOpen sets specific window state`() = runTest(testDispatcher) {
        viewModel.setWindowOpen(true)
        assertTrue(viewModel.isWindowOpen.value)

        viewModel.setWindowOpen(false)
        assertFalse(viewModel.isWindowOpen.value)
    }

    // === Stats Display Tests ===

    @Test
    fun `toggleStats changes stats display state`() = runTest(testDispatcher) {
        val initialState = viewModel.showStats.value

        viewModel.toggleStats()

        assertEquals(!initialState, viewModel.showStats.value)
    }

    // === Item Management Tests ===

    @Test
    fun `addItem adds item to active items`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")

        assertEquals(1, viewModel.activeItems.size)
        assertEquals("Test Item", viewModel.activeItems[0].title)
        assertEquals("Item", viewModel.activeItems[0].type)
    }

    @Test
    fun `addItem with isBackground replaces existing background`() = runTest(testDispatcher) {
        viewModel.addItem("Background 1", "Map", isBackground = true)
        viewModel.addItem("Background 2", "Map", isBackground = true)

        assertEquals(1, viewModel.activeItems.size)
        assertEquals("Background 2", viewModel.activeItems[0].title)
        assertTrue(viewModel.activeItems[0].isBackground)
    }

    @Test
    fun `addItem with combat stats sets hp and armor class`() = runTest(testDispatcher) {
        viewModel.addItem(
            title = "Fighter",
            type = "Character",
            currentHp = 15,
            maxHp = 20,
            armorClass = 18
        )

        val item = viewModel.activeItems[0]
        assertEquals(15, item.currentHp)
        assertEquals(20, item.maxHp)
        assertEquals(18, item.armorClass)
    }

    @Test
    fun `updatePosition updates item position`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.updatePosition(itemId, 500f, 300f)

        assertEquals(500f, viewModel.activeItems[0].x)
        assertEquals(300f, viewModel.activeItems[0].y)
    }

    @Test
    fun `updatePosition coerces position to valid range`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.updatePosition(itemId, 5000f, -5000f)

        // Should be coerced to -2000 to 2000 range
        assertEquals(2000f, viewModel.activeItems[0].x)
        assertEquals(-2000f, viewModel.activeItems[0].y)
    }

    @Test
    fun `updateSize updates item dimensions`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.updateSize(itemId, 300f, 400f)

        assertEquals(300f, viewModel.activeItems[0].width)
        assertEquals(400f, viewModel.activeItems[0].height)
    }

    @Test
    fun `updateZoom updates item zoom level`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.updateZoom(itemId, 0.5f)

        assertEquals(1.5f, viewModel.activeItems[0].zoom) // 1.0 + 0.5
    }

    @Test
    fun `updateZoom coerces zoom to valid range`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        // Try to set zoom beyond limits
        viewModel.updateZoom(itemId, 15f)

        // Should be coerced to 1.0 to 10.0 range
        assertEquals(10f, viewModel.activeItems[0].zoom)
    }

    @Test
    fun `updateOffset updates item offset`() = runTest(testDispatcher) {
        viewModel.addItem("Test Item", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.updateOffset(itemId, 0.3f, -0.2f)

        assertEquals(0.3f, viewModel.activeItems[0].offsetX)
        assertEquals(-0.2f, viewModel.activeItems[0].offsetY)
    }

    @Test
    fun `updateHp updates item hp and saves to repository`() = runTest(testDispatcher) {
        fakeRepository.saveCharacterResult = Result.Success(Unit)
        val character = Character(
            id = "char-1",
            name = "Test Character",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 1,
            description = "Test",
            maxHp = 20,
            currentHp = 15,
        )
        fakeRepository.saveCharacter(character)

        viewModel.addItem(
            title = "Test Character",
            type = "Character",
            sourceId = "char-1",
            currentHp = 15,
            maxHp = 20
        )
        val itemId = viewModel.activeItems[0].id

        viewModel.updateHp(itemId, -5)

        assertEquals(10, viewModel.activeItems[0].currentHp)

        // Wait for debounced save
        kotlinx.coroutines.delay(1100)

        assertTrue(fakeRepository.saveCharacterCalls > 1) // 1 for initial save, 1 for update
    }

    @Test
    fun `updateHp coerces hp to valid range`() = runTest(testDispatcher) {
        viewModel.addItem(
            title = "Test Character",
            type = "Character",
            currentHp = 10,
            maxHp = 20
        )
        val itemId = viewModel.activeItems[0].id

        viewModel.updateHp(itemId, -15) // Would go below 0

        assertEquals(0, viewModel.activeItems[0].currentHp)

        viewModel.updateHp(itemId, 30) // Would go above max

        assertEquals(20, viewModel.activeItems[0].currentHp)
    }

    @Test
    fun `removeItem removes item from active items`() = runTest(testDispatcher) {
        viewModel.addItem("Item 1", "Item")
        viewModel.addItem("Item 2", "Item")
        val itemId = viewModel.activeItems[0].id

        viewModel.removeItem(itemId)

        assertEquals(1, viewModel.activeItems.size)
        assertEquals("Item 2", viewModel.activeItems[0].title)
    }

    @Test
    fun `clearItems removes all active items`() = runTest(testDispatcher) {
        viewModel.addItem("Item 1", "Item")
        viewModel.addItem("Item 2", "Item")

        viewModel.clearItems()

        assertTrue(viewModel.activeItems.isEmpty())
    }

    // === Event Management Tests ===

    @Test
    fun `saveCurrentEvent creates new event if id is null`() = runTest(testDispatcher) {
        viewModel.addItem("Token 1", "Character")

        viewModel.saveCurrentEvent("Battle Scene")

        assertNotNull(viewModel.activeEvent.value)
        assertEquals("Battle Scene", viewModel.activeEvent.value?.name)
        assertTrue(viewModel.activeEvent.value?.items?.isNotEmpty() == true)
    }

    @Test
    fun `saveCurrentEvent updates existing event if id is provided`() = runTest(testDispatcher) {
        viewModel.addItem("Token 1", "Character")

        viewModel.saveCurrentEvent("Battle Scene", "event-123")

        assertEquals("event-123", viewModel.activeEvent.value?.id)
        assertEquals("Battle Scene", viewModel.activeEvent.value?.name)
    }

    @Test
    fun `renameEvent updates event name`() = runTest(testDispatcher) {
        viewModel.addItem("Token 1", "Character")
        viewModel.saveCurrentEvent("Old Name", "event-123")

        viewModel.renameEvent("event-123", "New Name")

        assertEquals("New Name", viewModel.activeEvent.value?.name)
        assertEquals("New Name", viewModel.events.value.find { it.id == "event-123" }?.name)
    }

    @Test
    fun `saveAsNewEvent creates new event with new id`() = runTest(testDispatcher) {
        viewModel.addItem("Token 1", "Character")
        viewModel.saveCurrentEvent("Original Event")

        val originalId = viewModel.activeEvent.value?.id

        viewModel.saveAsNewEvent("Copied Event")

        assertEquals("Copied Event", viewModel.activeEvent.value?.name)
        // Should have different ID
        assertTrue(viewModel.activeEvent.value?.id != originalId)
    }

    @Test
    fun `loadEvent loads event items into active items`() = runTest(testDispatcher) {
        val event = GameEvent(
            id = "event-1",
            name = "Test Event",
            items = listOf(
                PresentedItem(id = "1", title = "Token 1", type = "Character"),
                PresentedItem(id = "2", title = "Token 2", type = "Monster")
            )
        )

        viewModel.loadEvent(event)

        assertEquals("Test Event", viewModel.activeEvent.value?.name)
        assertEquals(2, viewModel.activeItems.size)
        assertEquals("Token 1", viewModel.activeItems[0].title)
        assertEquals("Token 2", viewModel.activeItems[1].title)
    }

    @Test
    fun `deleteEvent removes event from events and clears if active`() = runTest(testDispatcher) {
        val event = GameEvent(
            id = "event-1",
            name = "Test Event",
            items = emptyList()
        )

        viewModel.loadEvent(event)
        viewModel.deleteEvent("event-1")

        assertNull(viewModel.activeEvent.value)
        assertFalse(viewModel.events.value.any { it.id == "event-1" })
    }

    @Test
    fun `createNewScene clears active event and items`() = runTest(testDispatcher) {
        viewModel.addItem("Token 1", "Character")
        viewModel.saveCurrentEvent("Test Scene", "event-1")

        viewModel.createNewScene()

        assertNull(viewModel.activeEvent.value)
        assertTrue(viewModel.activeItems.isEmpty())
    }

    // === Refresh Tests ===

    @Test
    fun `refreshAll reloads data from repository`() = runTest(testDispatcher) {
        // Wait for initial load
        kotlinx.coroutines.delay(100)

        val initialMonsterCount = viewModel.monsters.value.size
        assertEquals(2, initialMonsterCount)

        // Update repository data with a different number of monsters
        fakeRepository.getMonstersResult = Result.Success(listOf(
            createTestMonster("monster-1", "Goblin"),
            createTestMonster("monster-2", "Orc"),
            createTestMonster("monster-3", "Dragon")
        ))

        viewModel.refreshAll(force = true)

        // Wait for refresh
        kotlinx.coroutines.delay(100)

        assertEquals(3, viewModel.monsters.value.size)
        assertTrue(viewModel.monsters.value.any { it.id == "monster-3" })
    }

    // === Remote Update Tests ===

    @Test
    fun `remote update for character refreshes character in active items`() = runTest(testDispatcher) {
        val character = Character(
            id = "char-1",
            name = "Fighter",
            playerName = "Player",
            race = "Human",
            characterClass = "Fighter",
            level = 3,
            description = "Brave fighter",
            maxHp = 30,
            currentHp = 25,
        )

        fakeRepository.getCharacterResult = Result.Success(character)

        viewModel.addItem(
            title = "Fighter",
            type = "Character",
            sourceId = "char-1",
            currentHp = 20,
            maxHp = 25
        )

        // Simulate remote update
        fakeRepository.emitRemoteUpdate("characters:char-1")

        // Wait for update processing
        kotlinx.coroutines.delay(100)

        val updatedItem = viewModel.activeItems.find { it.sourceId == "char-1" }
        assertEquals(25, updatedItem?.currentHp) // Updated from character
        assertEquals(30, updatedItem?.maxHp)
    }

    // === Edge Cases ===

    @Test
    fun `updatePosition on non-existent item does nothing`() = runTest(testDispatcher) {
        viewModel.updatePosition("non-existent", 100f, 200f)

        // Should not crash and state should remain unchanged
        assertTrue(viewModel.activeItems.isEmpty())
    }

    @Test
    fun `updateHp on item without sourceId does not attempt save`() = runTest(testDispatcher) {
        viewModel.addItem(
            title = "Test Item",
            type = "Item",
            currentHp = 10,
            maxHp = 20
        )
        val itemId = viewModel.activeItems[0].id

        viewModel.updateHp(itemId, 5)

        // Should update local state but not attempt save
        assertEquals(15, viewModel.activeItems[0].currentHp)
    }

    @Test
    fun `handles empty repository data gracefully`() = runTest(testDispatcher) {
        fakeRepository.getMonstersResult = Result.Success(emptyList())
        fakeRepository.getNpcsResult = Result.Success(emptyList())
        fakeRepository.getLocationsResult = Result.Success(emptyList())

        val newViewModel = PresentationViewModel(repository = fakeRepository)

        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(newViewModel.monsters.value.isEmpty())
        assertTrue(newViewModel.npcs.value.isEmpty())
        assertTrue(newViewModel.locations.value.isEmpty())
    }
}
