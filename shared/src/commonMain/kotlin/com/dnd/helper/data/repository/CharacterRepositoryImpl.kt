package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.repository.CharacterRepository

class CharacterRepositoryImpl(
    private val dataSource: GoogleAppsScriptDataSource,
) : CharacterRepository {

    override suspend fun getCharacters(): Result<List<Character>> {
        return dataSource.getCharacters()
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        return dataSource.getCharacter(id)
    }

    override suspend fun saveCharacter(character: Character): Result<Unit> {
        return dataSource.saveCharacter(character)
    }

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        return dataSource.deleteCharacter(id)
    }

    override suspend fun getLocations(): Result<List<Location>> {
        return dataSource.getLocations()
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        return dataSource.saveLocation(location)
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        return dataSource.deleteLocation(id)
    }

    override suspend fun getLastModified(): Result<String> {
        return dataSource.getLastModified()
    }
}
