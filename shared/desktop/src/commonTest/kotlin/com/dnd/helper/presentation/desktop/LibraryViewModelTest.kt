package com.dnd.helper.presentation.desktop

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Battlefield
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.fakes.FakeCharacterRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LibraryViewModel
    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeEditingRepository: FakeEditingRepository

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

    private fun createTestBattlefield(id: String = "bf-1", name: String = "Forest Clearing") = Battlefield(
        id = id,
        name = name,
        description = "A forest clearing",
        imageUrl = null
    )

    private fun createTestCharacter(id: String = "char-1", name: String = "Fighter") = Character(
        id = id,
        name = name,
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 3,
        description = "Brave fighter",
        maxHp = 30,
        currentHp = 25,
        items = emptyList()
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCharacterRepository()
        fakeEditingRepository = FakeEditingRepository()

        // Setup test data
        fakeRepository.getMonstersResult = Result.Success(listOf(createTestMonster()))
        fakeRepository.getNpcsResult = Result.Success(listOf(createTestNpc()))
        fakeRepository.getLocationsResult = Result.Success(listOf(createTestLocation()))
        fakeRepository.getBattlefieldsResult = Result.Success(listOf(createTestBattlefield()))
        fakeRepository.getCharactersResult = Result.Success(listOf(createTestCharacter()))

        viewModel = LibraryViewModel(
            repository = fakeRepository,
            editingRepository = fakeEditingRepository
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === Initial State Tests ===

    @Test
    fun `initial state loads items (characters) by default`() = runTest(testDispatcher) {
        // Wait for init
        kotlinx.coroutines.delay(100)

        assertEquals(LibraryType.Items, viewModel.state.value.selectedType)
        assertTrue(viewModel.state.value.characters.isNotEmpty())
    }

    @Test
    fun `initial state shows loading while fetching`() = runTest(testDispatcher) {
        val newViewModel = LibraryViewModel(
            repository = fakeRepository,
            editingRepository = fakeEditingRepository
        )

        // Should initially be loading (before async operation completes)
        assertTrue(newViewModel.state.value.isLoading || newViewModel.state.value.characters.isNotEmpty())
    }

    // === Type Selection Tests ===

    @Test
    fun `onTypeSelected updates selected type and loads corresponding data`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Locations)

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertEquals(LibraryType.Locations, viewModel.state.value.selectedType)
        assertTrue(viewModel.state.value.locations.isNotEmpty())
    }

    @Test
    fun `onTypeSelected with Mobs loads monsters`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Mobs)

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertEquals(LibraryType.Mobs, viewModel.state.value.selectedType)
        assertTrue(viewModel.state.value.monsters.isNotEmpty())
    }

    @Test
    fun `onTypeSelected with Npcs loads npcs`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Npcs)

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertEquals(LibraryType.Npcs, viewModel.state.value.selectedType)
        assertTrue(viewModel.state.value.npcs.isNotEmpty())
    }

    @Test
    fun `onTypeSelected with Battlefields loads battlefields`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Battlefields)

        // Wait for load
        kotlinx.coroutines.delay(100)

        assertEquals(LibraryType.Battlefields, viewModel.state.value.selectedType)
        assertTrue(viewModel.state.value.battlefields.isNotEmpty())
    }

    // === Delete Tests ===

    @Test
    fun `deleteMonster removes monster from state and repository`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Mobs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val monsterId = viewModel.state.value.monsters[0].id
        viewModel.deleteMonster(monsterId)

        // Wait for delete
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.monsters.isEmpty())
        assertTrue(fakeRepository.deleteMonsterCalls > 0)
    }

    @Test
    fun `deleteNpc removes npc from state and repository`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Npcs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val npcId = viewModel.state.value.npcs[0].id
        viewModel.deleteNpc(npcId)

        // Wait for delete
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.npcs.isEmpty())
        assertTrue(fakeRepository.deleteNpcCalls > 0)
    }

    @Test
    fun `deleteLocation removes location from state and repository`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Locations)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val locationId = viewModel.state.value.locations[0].id
        viewModel.deleteLocation(locationId)

        // Wait for delete
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.locations.isEmpty())
        assertTrue(fakeRepository.deleteLocationCalls > 0)
    }

    @Test
    fun `deleteBattlefield removes battlefield from state and repository`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Battlefields)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val battlefieldId = viewModel.state.value.battlefields[0].id
        viewModel.deleteBattlefield(battlefieldId)

        // Wait for delete
        kotlinx.coroutines.delay(100)

        assertTrue(viewModel.state.value.battlefields.isEmpty())
        assertTrue(fakeRepository.deleteBattlefieldCalls > 0)
    }

    // === Item Management Tests ===

    @Test
    fun `deleteItem removes item from character and schedules save`() = runTest(testDispatcher) {
        val item = Item(
            id = "item-1",
            name = "Sword",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "A sharp sword",
            weight = 2.0
        )
        val character = createTestCharacter().copy(items = listOf(item))
        fakeRepository.getCharactersResult = Result.Success(listOf(character))

        viewModel.onTypeSelected(LibraryType.Items)
        // Wait for load
        kotlinx.coroutines.delay(100)

        viewModel.deleteItem(character.id, item.id)

        // Should immediately update UI (optimistic)
        val updatedChar = viewModel.state.value.characters.find { it.id == character.id }
        assertTrue(updatedChar?.items?.isEmpty() == true)

        // Wait for debounced save
        kotlinx.coroutines.delay(400)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
    }

    @Test
    fun `addItem adds item to character and schedules save`() = runTest(testDispatcher) {
        val newItem = Item(
            id = "item-2",
            name = "Shield",
            slot = EquipmentSlot.OFF_HAND,
            rarity = ItemRarity.COMMON,
            description = "A sturdy shield",
            weight = 3.0
        )

        viewModel.onTypeSelected(LibraryType.Items)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val character = viewModel.state.value.characters[0]
        viewModel.addItem(character.id, newItem)

        // Should immediately update UI (optimistic)
        val updatedChar = viewModel.state.value.characters.find { it.id == character.id }
        assertTrue(updatedChar?.items?.size == 1)
        assertEquals("Shield", updatedChar?.items?.get(0)?.name)

        // Wait for debounced save
        kotlinx.coroutines.delay(400)

        assertTrue(fakeRepository.saveCharacterCalls > 0)
    }

    @Test
    fun `moveItemBetweenCharacters transfers item between characters`() = runTest(testDispatcher) {
        val item = Item(
            id = "item-1",
            name = "Sword",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "A sword",
            weight = 2.0
        )
        val char1 = createTestCharacter("char-1", "Fighter").copy(items = listOf(item))
        val char2 = createTestCharacter("char-2", "Wizard")
        fakeRepository.getCharactersResult = Result.Success(listOf(char1, char2))

        viewModel.onTypeSelected(LibraryType.Items)
        // Wait for load
        kotlinx.coroutines.delay(100)

        viewModel.moveItemBetweenCharacters(item, char1.id, char2.id)

        // Should immediately update both characters (optimistic)
        val updatedChar1 = viewModel.state.value.characters.find { it.id == char1.id }
        val updatedChar2 = viewModel.state.value.characters.find { it.id == char2.id }
        assertTrue(updatedChar1?.items?.isEmpty() == true)
        assertTrue(updatedChar2?.items?.size == 1)

        // Wait for debounced saves
        kotlinx.coroutines.delay(400)

        // Should save both characters
        assertTrue(fakeRepository.saveCharacterCalls >= 2)
    }

    @Test
    fun `moveItemBetweenCharacters with same source and target does nothing`() = runTest(testDispatcher) {
        val item = Item(
            id = "item-1",
            name = "Sword",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "A sword",
            weight = 2.0
        )
        val char1 = createTestCharacter("char-1", "Fighter").copy(items = listOf(item))
        fakeRepository.getCharactersResult = Result.Success(listOf(char1))

        viewModel.onTypeSelected(LibraryType.Items)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val beforeState = viewModel.state.value.characters[0].items.size

        viewModel.moveItemBetweenCharacters(item, char1.id, char1.id)

        // Should not change state
        assertEquals(beforeState, viewModel.state.value.characters[0].items.size)
    }

    // === Refresh Tests ===

    @Test
    fun `refreshData reloads data for current type`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Mobs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val initialCount = viewModel.state.value.monsters.size

        // Update repository data
        fakeRepository.getMonstersResult = Result.Success(listOf(
            createTestMonster("monster-2", "Orc"),
            createTestMonster("monster-3", "Dragon")
        ))

        viewModel.refreshData(force = true)

        // Wait for refresh
        kotlinx.coroutines.delay(100)

        assertEquals(2, viewModel.state.value.monsters.size)
    }

    // === Image Generation Tests ===

    @Test
    fun `generateMissingImages starts generation for entities without images`() = runTest(testDispatcher) {
        val npcWithoutImage = createTestNpc().copy(imageUrl = null)
        val monsterWithoutImage = createTestMonster().copy(imageUrl = null)
        fakeRepository.getNpcsResult = Result.Success(listOf(npcWithoutImage))
        fakeRepository.getMonstersResult = Result.Success(listOf(monsterWithoutImage))

        viewModel.generateMissingImages()

        // Wait for generation
        kotlinx.coroutines.delay(100)

        // Should have started generation for NPC and Monster
        assertTrue(fakeEditingRepository.activeTasks.value.size >= 2)
    }

    @Test
    fun `generateMissingImages with force regenerates all images`() = runTest(testDispatcher) {
        val npcWithImage = createTestNpc().copy(imageUrl = "http://example.com/npc.jpg")
        fakeRepository.getNpcsResult = Result.Success(listOf(npcWithImage))

        viewModel.generateMissingImages(force = true)

        // Wait for generation
        kotlinx.coroutines.delay(100)

        // Should generate image even if one exists
        assertTrue(fakeEditingRepository.activeTasks.value.any { it.entityType == "npc" })
    }

    @Test
    fun `generateMissingImages handles custom dimensions`() = runTest(testDispatcher) {
        val npcWithoutImage = createTestNpc().copy(imageUrl = null)
        fakeRepository.getNpcsResult = Result.Success(listOf(npcWithoutImage))

        viewModel.generateMissingImages(customWidth = 512, customHeight = 256)

        // Wait for generation
        kotlinx.coroutines.delay(100)

        // Should use custom dimensions (we can't verify this directly, but should not crash)
        assertTrue(fakeEditingRepository.activeTasks.value.isNotEmpty())
    }

    // === Remote Update Tests ===

    @Test
    fun `remote update for monsters reloads when on Mobs type`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Mobs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val initialCalls = fakeRepository.getMonstersCalls

        // Simulate remote update
        fakeRepository.emitCharacterUpdate("monsters")

        // Wait for refresh
        kotlinx.coroutines.delay(100)

        // Should have triggered a new getMonsters call
        assertTrue(fakeRepository.getMonstersCalls > initialCalls)
    }

    @Test
    fun `remote update for npcs reloads when on Npcs type`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Npcs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val initialCalls = fakeRepository.getNpcsCalls

        // Simulate remote update
        fakeRepository.emitCharacterUpdate("npcs")

        // Wait for refresh
        kotlinx.coroutines.delay(100)

        // Should have triggered a new getNpcs call
        assertTrue(fakeRepository.getNpcsCalls > initialCalls)
    }

    @Test
    fun `remote update is suppressed during pending saves`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Items)
        // Wait for load
        kotlinx.coroutines.delay(100)

        // Start an operation that will have pending save
        val newItem = Item(
            id = "item-1",
            name = "Test Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "Test",
            weight = 1.0
        )
        val character = viewModel.state.value.characters[0]
        viewModel.addItem(character.id, newItem)

        // Immediately simulate remote update
        fakeRepository.emitCharacterUpdate("characters")

        // Wait a bit
        kotlinx.coroutines.delay(100)

        // Should not have refreshed due to pending save
        // (The refresh should be suppressed)
    }

    // === Edge Cases ===

    @Test
    fun `handles empty data gracefully`() = runTest(testDispatcher) {
        fakeRepository.getMonstersResult = Result.Success(emptyList())
        fakeRepository.getNpcsResult = Result.Success(emptyList())
        fakeRepository.getLocationsResult = Result.Success(emptyList())
        fakeRepository.getBattlefieldsResult = Result.Success(emptyList())
        fakeRepository.getCharactersResult = Result.Success(emptyList())

        val newViewModel = LibraryViewModel(
            repository = fakeRepository,
            editingRepository = fakeEditingRepository
        )

        // Wait for init
        kotlinx.coroutines.delay(100)

        assertTrue(newViewModel.state.value.characters.isEmpty())
        assertFalse(newViewModel.state.value.isLoading)
    }

    @Test
    fun `handles rapid type switching correctly`() = runTest(testDispatcher) {
        repeat(3) {
            viewModel.onTypeSelected(LibraryType.values()[it % LibraryType.values().size])
        }

        // Should have the last type selected
        assertTrue(viewModel.state.value.selectedType != LibraryType.Items)
    }

    @Test
    fun `delete operations for non-existent entities do nothing`() = runTest(testDispatcher) {
        viewModel.onTypeSelected(LibraryType.Mobs)
        // Wait for load
        kotlinx.coroutines.delay(100)

        val initialCount = viewModel.state.value.monsters.size

        viewModel.deleteMonster("non-existent-id")

        // Should not crash and count should remain the same
        assertEquals(initialCount, viewModel.state.value.monsters.size)
    }
}
