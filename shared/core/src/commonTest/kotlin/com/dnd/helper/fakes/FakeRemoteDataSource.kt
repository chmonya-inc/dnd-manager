package com.dnd.helper.fakes

import com.dnd.helper.data.remote.RemoteDataSource
import com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto
import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.data.remote.dto.auth.MyCharactersResponse
import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Battlefield
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.GameEvent
import com.dnd.helper.domain.model.InitialData
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.LogEntry
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.MusicTrack
import com.dnd.helper.domain.model.Npc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * RemoteDataSource fake for repository tests. Only the endpoints exercised by repository
 * coordination logic (characters, locations, initial-data) are made controllable; the rest
 * return harmless success/empty defaults so the interface is fully satisfied.
 *
 * [updates] is the backing flow for [observeUpdates] — emit WS-style messages here
 * ("characters", "characters:id", "monsters", ...) to drive the repository's remote-update path.
 */
class FakeRemoteDataSource : RemoteDataSource {

    val updates = MutableSharedFlow<String>(extraBufferCapacity = 64)

    var getCharactersResult: Result<List<Character>> = Result.Success(emptyList())
    var getCharactersCalls = 0
        private set

    var getCharacterResult: Result<Character> = Result.Error(AppError.NotFound)
    var getCharacterCalls = 0
        private set

    var saveCharacterResult: Result<Unit> = Result.Success(Unit)
    var saveCharacterCalls = 0
        private set

    var deleteCharacterResult: Result<Unit> = Result.Success(Unit)
    var deleteCharacterCalls = 0
        private set

    var getLocationsResult: Result<List<Location>> = Result.Success(emptyList())
    var getLocationsCalls = 0
        private set

    var saveLocationResult: Result<Unit> = Result.Success(Unit)
    var saveLocationCalls = 0
        private set

    var deleteLocationResult: Result<Unit> = Result.Success(Unit)
    var deleteLocationCalls = 0
        private set

    var getInitialDataResult: Result<InitialData> = Result.Success(
        InitialData(
            characters = emptyList(),
            locations = emptyList(),
            monsters = emptyList(),
            npcs = emptyList(),
            lastModified = "",
        )
    )
    var getInitialDataCalls = 0
        private set

    override fun observeUpdates(): Flow<String> = updates

    override suspend fun getInitialData(): Result<InitialData> {
        getInitialDataCalls++
        return getInitialDataResult
    }

    override suspend fun getCharacters(): Result<List<Character>> {
        getCharactersCalls++
        return getCharactersResult
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        getCharacterCalls++
        return getCharacterResult
    }

    override suspend fun saveCharacter(character: Character): Result<Unit> {
        saveCharacterCalls++
        return saveCharacterResult
    }

    override suspend fun saveCharacterToSession(character: Character, targetSessionId: String): Result<Unit> =
        Result.Success(Unit)

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        deleteCharacterCalls++
        return deleteCharacterResult
    }

    override suspend fun deleteCharacterFromSession(id: String, sourceSessionId: String): Result<Unit> =
        Result.Success(Unit)

    override suspend fun getLocations(): Result<List<Location>> {
        getLocationsCalls++
        return getLocationsResult
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        saveLocationCalls++
        return saveLocationResult
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        deleteLocationCalls++
        return deleteLocationResult
    }

    // --- Defaults for the remaining endpoints (not exercised by repository tests) ---

    override suspend fun getBattlefields(): Result<List<Battlefield>> = Result.Success(emptyList())
    override suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteBattlefield(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getMonsters(): Result<List<Monster>> = Result.Success(emptyList())
    override suspend fun saveMonster(monster: Monster): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteMonster(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getNpcs(): Result<List<Npc>> = Result.Success(emptyList())
    override suspend fun saveNpc(npc: Npc): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteNpc(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getMusic(): Result<List<MusicTrack>> = Result.Success(emptyList())
    override suspend fun saveMusic(music: MusicTrack): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteMusic(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getLogs(): Result<List<LogEntry>> = Result.Success(emptyList())
    override suspend fun saveLog(log: LogEntry): Result<Unit> = Result.Success(Unit)

    override suspend fun getEvents(): Result<List<GameEvent>> = Result.Success(emptyList())
    override suspend fun saveEvent(event: GameEvent): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteEvent(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun assignCharacter(characterId: String, sessionId: String, ownerUserId: String?): Result<Unit> =
        Result.Success(Unit)
    override suspend fun assignCharacterByUsername(characterId: String, sessionId: String, username: String?): Result<Unit> =
        Result.Success(Unit)

    override suspend fun getMyCharacters(): Result<MyCharactersResponse> =
        Result.Success(MyCharactersResponse(emptyList(), emptyList()))
    override suspend fun getMyCharacter(characterId: String): Result<Character> = Result.Error(AppError.NotFound)
    override suspend fun createMyCharacter(character: Character): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteMyCharacter(characterId: String): Result<Unit> = Result.Success(Unit)
    override suspend fun joinCampaign(characterId: String, gameId: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getCampaigns(): Result<List<CampaignDto>> = Result.Success(emptyList())
    override suspend fun toggleCampaignStart(campaignId: String, isStarted: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun createCampaign(name: String, sessionId: String): Result<CampaignDto> =
        Result.Error(AppError.Unknown("not used"))

    override suspend fun createAssignment(characterId: String, sessionId: String, playerUsername: String): Result<Unit> =
        Result.Success(Unit)
    override suspend fun getPendingAssignments(): Result<List<PendingAssignmentDto>> = Result.Success(emptyList())
    override suspend fun respondToAssignment(assignmentId: String, accept: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun getAssignmentStatuses(sessionId: String): Result<List<AssignmentStatusDto>> =
        Result.Success(emptyList())
}
