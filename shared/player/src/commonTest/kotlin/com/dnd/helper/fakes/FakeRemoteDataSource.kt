package com.dnd.helper.fakes

import com.dnd.helper.data.remote.RemoteDataSource
import com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto
import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.data.remote.dto.auth.MyCharactersResponse
import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto
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

class FakeRemoteDataSource : RemoteDataSource {
    val updates = MutableSharedFlow<String>()
    
    var myCharactersResponse = MyCharactersResponse(emptyList(), emptyList())
    var pendingAssignments = emptyList<PendingAssignmentDto>()
    
    var joinCampaignResult: Result<Unit> = Result.Success(Unit)
    var deleteCharacterResult: Result<Unit> = Result.Success(Unit)
    var respondToAssignmentResult: Result<Unit> = Result.Success(Unit)

    override fun observeUpdates(): Flow<String> = updates
    override suspend fun getMyCharacters(): Result<MyCharactersResponse> = Result.Success(myCharactersResponse)
    override suspend fun getPendingAssignments(): Result<List<PendingAssignmentDto>> = Result.Success(pendingAssignments)
    
    override suspend fun joinCampaign(characterId: String, gameId: String): Result<Unit> = joinCampaignResult
    override suspend fun deleteMyCharacter(characterId: String): Result<Unit> = deleteCharacterResult
    override suspend fun respondToAssignment(assignmentId: String, accept: Boolean): Result<Unit> = respondToAssignmentResult

    // Implement other methods as needed with default empty/success results
    override suspend fun getInitialData(): Result<InitialData> = Result.Error(com.dnd.helper.domain.common.AppError.Unknown("Not implemented in fake"))
    override suspend fun getCharacters(): Result<List<Character>> = Result.Success(emptyList())
    override suspend fun getCharacter(id: String): Result<Character> = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)
    override suspend fun saveCharacter(character: Character): Result<Unit> = Result.Success(Unit)
    override suspend fun saveCharacterToSession(character: Character, targetSessionId: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteCharacter(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteCharacterFromSession(id: String, sourceSessionId: String): Result<Unit> = Result.Success(Unit)
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
    override suspend fun assignCharacterByUsername(characterId: String, sessionId: String, username: String?): Result<Unit> = Result.Success(Unit)
    override suspend fun getMyCharacter(characterId: String): Result<Character> = Result.Error(com.dnd.helper.domain.common.AppError.NotFound)
    override suspend fun createMyCharacter(character: Character): Result<Unit> = Result.Success(Unit)
    override suspend fun getCampaigns(): Result<List<CampaignDto>> = Result.Success(emptyList())
    override suspend fun toggleCampaignStart(campaignId: String, isStarted: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun createCampaign(name: String, sessionId: String): Result<CampaignDto> = Result.Error(com.dnd.helper.domain.common.AppError.Unknown(""))
    override suspend fun createAssignment(characterId: String, sessionId: String, playerUsername: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getAssignmentStatuses(sessionId: String): Result<List<AssignmentStatusDto>> = Result.Success(emptyList())
}
