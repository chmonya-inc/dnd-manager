package com.dnd.helper.data.remote

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

interface RemoteDataSource {
    fun observeUpdates(): Flow<String>
    suspend fun getInitialData(): Result<InitialData>
    suspend fun getCharacters(): Result<List<Character>>
    suspend fun getCharacter(id: String): Result<Character>
    suspend fun saveCharacter(character: Character): Result<Unit>
    suspend fun saveCharacterToSession(character: Character, targetSessionId: String): Result<Unit>
    suspend fun deleteCharacter(id: String): Result<Unit>
    suspend fun deleteCharacterFromSession(id: String, sourceSessionId: String): Result<Unit>
    suspend fun getLocations(): Result<List<Location>>
    suspend fun saveLocation(location: Location): Result<Unit>
    suspend fun deleteLocation(id: String): Result<Unit>
    suspend fun getBattlefields(): Result<List<Battlefield>>
    suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit>
    suspend fun deleteBattlefield(id: String): Result<Unit>
    suspend fun getMonsters(): Result<List<Monster>>
    suspend fun saveMonster(monster: Monster): Result<Unit>
    suspend fun deleteMonster(id: String): Result<Unit>
    suspend fun getNpcs(): Result<List<Npc>>
    suspend fun saveNpc(npc: Npc): Result<Unit>
    suspend fun deleteNpc(id: String): Result<Unit>
    suspend fun getMusic(): Result<List<MusicTrack>>
    suspend fun saveMusic(music: MusicTrack): Result<Unit>
    suspend fun deleteMusic(id: String): Result<Unit>
    suspend fun getLogs(): Result<List<LogEntry>>
    suspend fun saveLog(log: LogEntry): Result<Unit>
    suspend fun getEvents(): Result<List<GameEvent>>
    suspend fun saveEvent(event: GameEvent): Result<Unit>
    suspend fun deleteEvent(id: String): Result<Unit>

    suspend fun assignCharacter(characterId: String, sessionId: String, ownerUserId: String?): Result<Unit>
    suspend fun assignCharacterByUsername(characterId: String, sessionId: String, username: String?): Result<Unit>
    suspend fun getMyCharacters(): Result<MyCharactersResponse>
    suspend fun getMyCharacter(characterId: String): Result<Character>
    suspend fun createMyCharacter(character: Character): Result<Unit>
    suspend fun deleteMyCharacter(characterId: String): Result<Unit>
    suspend fun joinCampaign(characterId: String, gameId: String): Result<Unit>
    suspend fun getCampaigns(): Result<List<CampaignDto>>
    suspend fun toggleCampaignStart(campaignId: String, isStarted: Boolean): Result<Unit>
    suspend fun createCampaign(name: String, sessionId: String): Result<CampaignDto>

    suspend fun createAssignment(characterId: String, sessionId: String, playerUsername: String): Result<Unit>
    suspend fun getPendingAssignments(): Result<List<PendingAssignmentDto>>
    suspend fun respondToAssignment(assignmentId: String, accept: Boolean): Result<Unit>
    suspend fun getAssignmentStatuses(sessionId: String): Result<List<AssignmentStatusDto>>
}
