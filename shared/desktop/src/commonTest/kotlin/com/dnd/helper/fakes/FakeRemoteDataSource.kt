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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** Minimal RemoteDataSource fake tracking the assignment/character endpoints. */
class FakeRemoteDataSource : RemoteDataSource {

    val updates = MutableSharedFlow<String>(extraBufferCapacity = 16)

    var createAssignmentCalls = 0
    var createMyCharacterCalls = 0
    var deleteMyCharacterCalls = 0
    var unassignByUsernameCalls = 0
    var saveCharacterToSessionCalls = 0
    var lastSavedToSessionCharacter: Character? = null
    var lastSaveTargetSession: String? = null
    var deleteCharacterFromSessionCalls = 0
    var lastDeleteFromSessionId: String? = null
    var lastDeleteFromSession: String? = null
    var saveToSessionResult: Result<Unit> = Result.Success(Unit)
    var deleteFromSessionResult: Result<Unit> = Result.Success(Unit)

    /** When non-null, createAssignment() suspends on this until completed — used to keep a
     *  coroutine in-flight (isAssigning == true) so re-entry guards can be exercised. */
    var createAssignmentGate: CompletableDeferred<Unit>? = null

    var createAssignmentResult: Result<Unit> = Result.Success(Unit)
    var assignByUsernameResult: Result<Unit> = Result.Success(Unit)
    var statuses: List<AssignmentStatusDto> = emptyList()
    var myCharactersResponse = MyCharactersResponse(emptyList(), emptyList())
    var campaignsResult: Result<List<CampaignDto>> = Result.Success(emptyList())
    var getInitialDataResult: Result<InitialData> = Result.Success(InitialData(
        characters = emptyList(),
        locations = emptyList(),
        monsters = emptyList(),
        npcs = emptyList(),
        lastModified = ""
    ))
    var toggleCampaignStartResult: Result<Unit> = Result.Success(Unit)
    var createCampaignResult: Result<CampaignDto> = Result.Error(AppError.Unknown("not implemented"))

    override fun observeUpdates(): Flow<String> = updates

    override suspend fun createAssignment(
        characterId: String,
        sessionId: String,
        playerUsername: String,
    ): Result<Unit> {
        createAssignmentCalls++
        createAssignmentGate?.await()
        return createAssignmentResult
    }

    override suspend fun assignCharacterByUsername(
        characterId: String,
        sessionId: String,
        username: String?,
    ): Result<Unit> {
        if (username == null) unassignByUsernameCalls++
        return assignByUsernameResult
    }

    override suspend fun getAssignmentStatuses(sessionId: String): Result<List<AssignmentStatusDto>> =
        Result.Success(statuses)

    override suspend fun getPendingAssignments(): Result<List<PendingAssignmentDto>> = Result.Success(emptyList())
    override suspend fun getMyCharacters(): Result<MyCharactersResponse> = Result.Success(myCharactersResponse)
    override suspend fun getMyCharacter(characterId: String): Result<Character> = Result.Error(AppError.NotFound)
    override suspend fun createMyCharacter(character: Character): Result<Unit> {
        createMyCharacterCalls++
        return Result.Success(Unit)
    }
    override suspend fun deleteMyCharacter(characterId: String): Result<Unit> {
        deleteMyCharacterCalls++
        return Result.Success(Unit)
    }

    // --- defaults for the rest ---
    override suspend fun getInitialData(): Result<InitialData> = getInitialDataResult
    override suspend fun getCharacters(): Result<List<Character>> = Result.Success(emptyList())
    override suspend fun getCharacter(id: String): Result<Character> = Result.Error(AppError.NotFound)
    override suspend fun saveCharacter(character: Character): Result<Unit> = Result.Success(Unit)
    override suspend fun saveCharacterToSession(character: Character, targetSessionId: String): Result<Unit> {
        saveCharacterToSessionCalls++
        lastSavedToSessionCharacter = character
        lastSaveTargetSession = targetSessionId
        return saveToSessionResult
    }
    override suspend fun deleteCharacter(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteCharacterFromSession(id: String, sourceSessionId: String): Result<Unit> {
        deleteCharacterFromSessionCalls++
        lastDeleteFromSessionId = id
        lastDeleteFromSession = sourceSessionId
        return deleteFromSessionResult
    }
    override suspend fun getLocations(): Result<List<Location>> = Result.Success(emptyList())
    override suspend fun saveLocation(location: Location): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteLocation(id: String): Result<Unit> = Result.Success(Unit)
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
    override suspend fun assignCharacter(characterId: String, sessionId: String, ownerUserId: String?): Result<Unit> = Result.Success(Unit)
    override suspend fun joinCampaign(characterId: String, gameId: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getCampaigns(): Result<List<CampaignDto>> = campaignsResult
    override suspend fun toggleCampaignStart(campaignId: String, isStarted: Boolean): Result<Unit> = toggleCampaignStartResult
    override suspend fun createCampaign(name: String, sessionId: String): Result<CampaignDto> = createCampaignResult
    override suspend fun respondToAssignment(assignmentId: String, accept: Boolean): Result<Unit> = Result.Success(Unit)
}
