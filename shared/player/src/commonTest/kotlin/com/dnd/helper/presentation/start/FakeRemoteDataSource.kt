package com.dnd.helper.fakes

import com.dnd.helper.data.remote.RemoteDataSource
import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.data.remote.dto.auth.MyCharactersResponse
import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character

/**
 * Enhanced FakeRemoteDataSource for testing ViewModels with controllable behavior
 */
class FakeRemoteDataSourceForStart : RemoteDataSource {

    // My Characters
    var getMyCharactersResult: Result<MyCharactersResponse> = Result.Success(MyCharactersResponse())
    var getMyCharactersCalls = 0
        private set

    // Pending Assignments
    var getPendingAssignmentsResult: Result<List<PendingAssignmentDto>> = Result.Success(emptyList())
    var getPendingAssignmentsCalls = 0
        private set

    // Assignment Response
    var respondToAssignmentResult: Result<Unit> = Result.Success(Unit)
    var respondToAssignmentCalls = 0
        private set

    // Join Campaign
    var joinCampaignResult: Result<Unit> = Result.Success(Unit)
    var joinCampaignCalls = 0
        private set

    // Delete Character
    var deleteMyCharacterResult: Result<Unit> = Result.Success(Unit)
    var deleteMyCharacterCalls = 0
        private set

    // Character operations
    var getMyCharacterResult: Result<Character> = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)
    var createMyCharacterResult: Result<Unit> = Result.Success(Unit)
    var createMyCharacterCalls = 0
        private set

    // Remote updates flow
    val remoteUpdates = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 64)

    override fun observeUpdates() = remoteUpdates

    override suspend fun getMyCharacters(): Result<MyCharactersResponse> {
        getMyCharactersCalls++
        return getMyCharactersResult
    }

    override suspend fun getPendingAssignments(): Result<List<PendingAssignmentDto>> {
        getPendingAssignmentsCalls++
        return getPendingAssignmentsResult
    }

    override suspend fun respondToAssignment(assignmentId: String, accept: Boolean): Result<Unit> {
        respondToAssignmentCalls++
        return respondToAssignmentResult
    }

    override suspend fun joinCampaign(characterId: String, gameId: String): Result<Unit> {
        joinCampaignCalls++
        return joinCampaignResult
    }

    override suspend fun deleteMyCharacter(characterId: String): Result<Unit> {
        deleteMyCharacterCalls++
        return deleteMyCharacterResult
    }

    override suspend fun getMyCharacter(characterId: String): Result<Character> {
        return getMyCharacterResult
    }

    override suspend fun createMyCharacter(character: Character): Result<Unit> {
        createMyCharacterCalls++
        return createMyCharacterResult
    }

    // Minimal implementations for other required methods
    override suspend fun getInitialData(): Result<com.dnd.helper.domain.model.InitialData> =
        Result.Error(com.dnd.helper.domain.common.AppError.NotFound)

    override suspend fun getCharacters(): Result<List<Character>> = Result.Success(emptyList())
    override suspend fun getCharacter(id: String): Result<Character> = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)
    override suspend fun saveCharacter(character: Character): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteCharacter(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getLocations(): Result<List<com.dnd.helper.domain.model.Location>> = Result.Success(emptyList())
    override suspend fun saveLocation(location: com.dnd.helper.domain.model.Location): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteLocation(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getBattlefields(): Result<List<com.dnd.helper.domain.model.Battlefield>> = Result.Success(emptyList())
    override suspend fun saveBattlefield(battlefield: com.dnd.helper.domain.model.Battlefield): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteBattlefield(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getMonsters(): Result<List<com.dnd.helper.domain.model.Monster>> = Result.Success(emptyList())
    override suspend fun saveMonster(monster: com.dnd.helper.domain.model.Monster): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteMonster(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getNpcs(): Result<List<com.dnd.helper.domain.model.Npc>> = Result.Success(emptyList())
    override suspend fun saveNpc(npc: com.dnd.helper.domain.model.Npc): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteNpc(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getMusic(): Result<List<com.dnd.helper.domain.model.MusicTrack>> = Result.Success(emptyList())
    override suspend fun saveMusic(music: com.dnd.helper.domain.model.MusicTrack): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteMusic(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getLogs(): Result<List<com.dnd.helper.domain.model.LogEntry>> = Result.Success(emptyList())
    override suspend fun saveLog(log: com.dnd.helper.domain.model.LogEntry): Result<Unit> = Result.Success(Unit)
    override suspend fun getEvents(): Result<List<com.dnd.helper.domain.model.GameEvent>> = Result.Success(emptyList())
    override suspend fun saveEvent(event: com.dnd.helper.domain.model.GameEvent): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteEvent(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun assignCharacter(characterId: String, sessionId: String, ownerUserId: String?): Result<Unit> = Result.Success(Unit)
    override suspend fun assignCharacterByUsername(characterId: String, sessionId: String, username: String?): Result<Unit> = Result.Success(Unit)
    override suspend fun createCampaign(name: String, sessionId: String): Result<CampaignDto> = Result.Error(com.dnd.helper.domain.common.AppError.Unknown("not used"))
    override suspend fun createAssignment(characterId: String, sessionId: String, playerUsername: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getAssignmentStatuses(sessionId: String): Result<List<com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto>> = Result.Success(emptyList())
    override suspend fun getCampaigns(): Result<List<CampaignDto>> = Result.Success(emptyList())
    override suspend fun toggleCampaignStart(campaignId: String, isStarted: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun saveCharacterToSession(character: com.dnd.helper.domain.model.Character, targetSessionId: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteCharacterFromSession(id: String, sourceSessionId: String): Result<Unit> = Result.Success(Unit)

    fun reset() {
        getMyCharactersResult = Result.Success(MyCharactersResponse())
        getMyCharactersCalls = 0
        getPendingAssignmentsResult = Result.Success(emptyList())
        getPendingAssignmentsCalls = 0
        respondToAssignmentResult = Result.Success(Unit)
        respondToAssignmentCalls = 0
        joinCampaignResult = Result.Success(Unit)
        joinCampaignCalls = 0
        deleteMyCharacterResult = Result.Success(Unit)
        deleteMyCharacterCalls = 0
    }
}
