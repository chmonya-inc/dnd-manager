package com.dnd.helper.data.repository

import app.cash.turbine.test
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.model.Location
import com.dnd.helper.fakes.FakeCharacterStorage
import com.dnd.helper.fakes.FakeRemoteDataSource
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dataSource: FakeRemoteDataSource
    private lateinit var storage: FakeCharacterStorage
    private lateinit var repository: CharacterRepositoryImpl

    private fun character(
        id: String,
        name: String = id,
        items: List<Item> = emptyList(),
    ) = Character(
        id = id,
        name = name,
        playerName = "P",
        race = "Elf",
        characterClass = "Mage",
        level = 1,
        description = "",
        maxHp = 10,
        currentHp = 10,
        items = items,
    )

    private val sword = Item(
        id = "i1",
        name = "Sword",
        slot = EquipmentSlot.MAIN_HAND,
        rarity = ItemRarity.COMMON,
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataSource = FakeRemoteDataSource()
        storage = FakeCharacterStorage().apply { saveTableId("session-1") }
        repository = CharacterRepositoryImpl(dataSource, storage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCharacters caches the result and does not refetch on a second call`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))

        val first = repository.getCharacters()
        val second = repository.getCharacters()

        assertIs<Result.Success<List<Character>>>(first)
        assertEquals(listOf("c1"), first.data.map { it.id })
        assertIs<Result.Success<List<Character>>>(second)
        assertEquals(first.data, second.data)
        assertEquals(1, dataSource.getCharactersCalls)
    }

    @Test
    fun `getCharacters with forceRefresh bypasses the cache`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))

        repository.getCharacters()
        repository.getCharacters(forceRefresh = true)

        assertEquals(2, dataSource.getCharactersCalls)
    }

    @Test
    fun `getCharacters filters out the ID placeholder row`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(
            listOf(character("ID", "placeholder"), character("c1", "real"))
        )

        val result = repository.getCharacters()

        assertIs<Result.Success<List<Character>>>(result)
        assertEquals(listOf("c1"), result.data.map { it.id })
    }

    @Test
    fun `getCharacter merges heavy detail into later light list entries`() = runTest(testDispatcher) {
        // 1. Prime the list cache with a light entry (empty items).
        dataSource.getCharactersResult = Result.Success(listOf(character("c1", name = "Light")))
        repository.getCharacters()

        // 2. A full detail fetch populates the heavy cache for c1 (with items).
        dataSource.getCharacterResult = Result.Success(character("c1", name = "Heavy", items = listOf(sword)))
        val detail = repository.getCharacter("c1")
        assertIs<Result.Success<Character>>(detail)
        assertEquals(listOf(sword), detail.data.items)

        // 3. The server now returns the light shape again; the repo must merge the heavy items back.
        dataSource.getCharactersResult = Result.Success(listOf(character("c1", name = "Light")))

        val result = repository.getCharacters(forceRefresh = true)

        assertIs<Result.Success<List<Character>>>(result)
        val merged = result.data.single()
        assertEquals("Light", merged.name) // identity comes from the fresh light entry
        assertEquals(listOf(sword), merged.items) // ...but items are preserved from the heavy cache
    }

    @Test
    fun `saveCharacter success updates the cache and emits the character id`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1", name = "old")))
        repository.getCharacters()

        repository.characterUpdates.test {
            // Replace an existing cached character.
            repository.saveCharacter(character("c1", name = "updated"))
            assertEquals("c1", awaitItem())

            // Append a brand-new character.
            repository.saveCharacter(character("c2", name = "new"))
            assertEquals("c2", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // Cache reflects both writes, and no extra network fetch happened.
        val cached = repository.getCharacters()
        assertIs<Result.Success<List<Character>>>(cached)
        assertEquals(listOf("c1" to "updated", "c2" to "new"), cached.data.map { it.id to it.name })
        assertEquals(1, dataSource.getCharactersCalls)
    }

    @Test
    fun `saveCharacter error leaves the cache untouched`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1", name = "original")))
        repository.getCharacters()

        dataSource.saveCharacterResult = Result.Error(AppError.Unknown("boom"))
        val result = repository.saveCharacter(character("c1", name = "should-not-stick"))

        assertIs<Result.Error>(result)
        val cached = repository.getCharacters()
        assertIs<Result.Success<List<Character>>>(cached)
        assertEquals(listOf("original"), cached.data.map { it.name })
    }

    @Test
    fun `optimisticUpdate updates the cache and emits without hitting the data source`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))
        repository.getCharacters()
        val saveCallsBefore = dataSource.saveCharacterCalls

        repository.characterUpdates.test {
            repository.optimisticUpdate(character("c1", name = "optimistic"))
            assertEquals("c1", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(saveCallsBefore, dataSource.saveCharacterCalls) // no network write
        val cached = repository.getCharacters()
        assertIs<Result.Success<List<Character>>>(cached)
        assertEquals("optimistic", cached.data.first { it.id == "c1" }.name) // cache updated immediately
    }

    @Test
    fun `deleteCharacter removes the character from the cache`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1"), character("c2")))
        repository.getCharacters()

        repository.deleteCharacter("c1")

        val cached = repository.getCharacters()
        assertIs<Result.Success<List<Character>>>(cached)
        assertEquals(listOf("c2"), cached.data.map { it.id })
    }

    @Test
    fun `changing the stored table id invalidates the cache and forces a refetch`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))
        repository.getCharacters()
        assertEquals(1, dataSource.getCharactersCalls)

        storage.saveTableId("session-2")

        repository.getCharacters()
        assertEquals(2, dataSource.getCharactersCalls)
    }

    @Test
    fun `a remote characters update invalidates the cache`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))
        repository.getCharacters()
        assertEquals(1, dataSource.getCharactersCalls)

        // Simulate a WebSocket "characters" broadcast.
        dataSource.updates.emit("characters")

        // Cache was invalidated → next read must refetch even without forceRefresh.
        repository.getCharacters()
        assertEquals(2, dataSource.getCharactersCalls)
    }

    @Test
    fun `remote update with an entity id routes the id through characterUpdates`() = runTest(testDispatcher) {
        dataSource.getCharactersResult = Result.Success(listOf(character("c1")))
        repository.getCharacters()

        repository.characterUpdates.test {
            dataSource.updates.emit("characters:c1")
            assertEquals("c1", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `locations are cached and invalidated on save and delete`() = runTest(testDispatcher) {
        dataSource.getLocationsResult = Result.Success(listOf(Location("l1", "Tavern", "desc")))

        repository.getLocations()
        repository.getLocations() // served from cache
        assertEquals(1, dataSource.getLocationsCalls)

        repository.saveLocation(Location("l2", "Shop", "desc"))
        repository.getLocations() // cache invalidated by save
        assertEquals(2, dataSource.getLocationsCalls)

        repository.deleteLocation("l1")
        repository.getLocations() // cache invalidated by delete
        assertEquals(3, dataSource.getLocationsCalls)
    }
}
