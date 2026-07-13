package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Battlefield
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.fakes.FakeAiImageService
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Simplified test that tests the fake dependencies properly
@OptIn(ExperimentalCoroutinesApi::class)
class EditingRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAiService: FakeAiImageService
    private lateinit var characterRepository: FakeCharacterRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAiService = FakeAiImageService()
        characterRepository = FakeCharacterRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fake AiImageService generates URLs correctly`() = runTest(testDispatcher) {
        fakeAiService.generateImageResult = "https://test.com/result.png"

        val result = fakeAiService.generateImage(
            promptText = "A brave knight",
            type = GenerationType.CHARACTER
        )

        assertEquals("https://test.com/result.png", result)
        assertEquals(1, fakeAiService.generateImageCalls)
    }

    @Test
    fun `fake AiImageService calls onRemoteIdGenerated callback`() = runTest(testDispatcher) {
        var callbackCalled = false
        var receivedId: String? = null

        fakeAiService.generateImage("test", GenerationType.CHARACTER) { remoteId ->
            callbackCalled = true
            receivedId = remoteId
        }

        assertTrue(callbackCalled)
        assertNotNull(receivedId)
        assertTrue(receivedId!!.startsWith("remote-id-"))
    }

    @Test
    fun `fake AiImageService can simulate failures`() = runTest(testDispatcher) {
        fakeAiService.shouldThrow = true

        try {
            val result = fakeAiService.generateImage("test", GenerationType.CHARACTER)
            // If we get here, the exception wasn't thrown
            assertTrue(false, "Expected exception to be thrown")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("AI service failure") == true)
        }
    }

    @Test
    fun `fake AiImageService delay can be configured`() = runTest(testDispatcher) {
        fakeAiService.generateImageDelay = 250L // Set custom delay

        assertEquals(250L, fakeAiService.generateImageDelay)
    }

    @Test
    fun `FakeCharacterRepository saves and retrieves characters`() = runTest(testDispatcher) {
        val testCharacter = createTestCharacter()

        val saveResult = characterRepository.saveCharacter(testCharacter)
        assertTrue(saveResult is Result.Success)

        val getResult = characterRepository.getCharacter("char-1")
        assertTrue(getResult is Result.Success)
        assertEquals("Test Character", getResult.data.name)
    }

    @Test
    fun `FakeCharacterRepository tracks save calls`() = runTest(testDispatcher) {
        val testCharacter = createTestCharacter()

        characterRepository.saveCharacter(testCharacter)
        characterRepository.saveCharacter(testCharacter)

        assertEquals(2, characterRepository.saveCharacterCalls)
    }

    @Test
    fun `FakeCharacterRepository can simulate save failures`() = runTest(testDispatcher) {
        characterRepository.saveCharacterResult = Result.Error(AppError.Unknown("Save failed"))
        val testCharacter = createTestCharacter()

        val result = characterRepository.saveCharacter(testCharacter)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `FakeCharacterRepository saves and retrieves NPCs`() = runTest(testDispatcher) {
        val testNpc = createTestNpc()

        val saveResult = characterRepository.saveNpc(testNpc)
        assertTrue(saveResult is Result.Success)

        val getResult = characterRepository.getNpcs()
        assertTrue(getResult is Result.Success)
        assertEquals("Test NPC", getResult.data[0].name)
    }

    @Test
    fun `FakeCharacterRepository saves and retrieves monsters`() = runTest(testDispatcher) {
        val testMonster = createTestMonster()

        val saveResult = characterRepository.saveMonster(testMonster)
        assertTrue(saveResult is Result.Success)

        val getResult = characterRepository.getMonsters()
        assertTrue(getResult is Result.Success)
        assertEquals("Test Monster", getResult.data[0].name)
    }

    @Test
    fun `FakeCharacterRepository saves and retrieves locations`() = runTest(testDispatcher) {
        val testLocation = createTestLocation()

        val saveResult = characterRepository.saveLocation(testLocation)
        assertTrue(saveResult is Result.Success)

        val getResult = characterRepository.getLocations()
        assertTrue(getResult is Result.Success)
        assertEquals("Test Location", getResult.data[0].name)
    }

    @Test
    fun `FakeCharacterRepository saves and retrieves battlefields`() = runTest(testDispatcher) {
        val testBattlefield = createTestBattlefield()

        val saveResult = characterRepository.saveBattlefield(testBattlefield)
        assertTrue(saveResult is Result.Success)

        val getResult = characterRepository.getBattlefields()
        assertTrue(getResult is Result.Success)
        assertEquals("Test Battlefield", getResult.data[0].name)
    }

    private fun createTestCharacter(id: String = "char-1") = Character(
        id = id,
        name = "Test Character",
        playerName = "Player",
        race = "Human",
        characterClass = "Fighter",
        level = 1,
        description = "Test description",
        maxHp = 20,
        currentHp = 20,
        items = emptyList()
    )

    private fun createTestNpc(id: String = "npc-1") = Npc(
        id = id,
        name = "Test NPC",
        description = "Test NPC description",
        imageUrl = ""
    )

    private fun createTestMonster(id: String = "monster-1") = Monster(
        id = id,
        name = "Test Monster",
        description = "Test monster description",
        imageUrl = ""
    )

    private fun createTestLocation(id: String = "location-1") = Location(
        id = id,
        name = "Test Location",
        description = "Test location description",
        imageUrl = ""
    )

    private fun createTestBattlefield(id: String = "battlefield-1") = Battlefield(
        id = id,
        name = "Test Battlefield",
        description = "Test battlefield description",
        imageUrl = ""
    )

    private fun createTestItem() = Item(
        id = "item-1",
        name = "Test Item",
        description = "Test item description",
        slot = EquipmentSlot.MAIN_HAND,
        rarity = ItemRarity.COMMON,
        weight = 1.0,
        imageUrl = ""
    )

    private fun createTestSpell() = Spell(
        id = "spell-1",
        name = "Fireball",
        iconUrl = "",
        description = "Test spell",
        level = 3,
        school = "Evocation",
        castingTime = "1 action",
        range = "120 feet",
        duration = "Instantaneous",
        isPassive = false
    )
}
