package com.dnd.helper.domain.repository

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character

interface CharacterRepository {
    suspend fun getCharacters(): Result<List<Character>>
    suspend fun getCharacter(id: String): Result<Character>
    suspend fun saveCharacter(character: Character): Result<Unit>
    suspend fun deleteCharacter(id: String): Result<Unit>
}
