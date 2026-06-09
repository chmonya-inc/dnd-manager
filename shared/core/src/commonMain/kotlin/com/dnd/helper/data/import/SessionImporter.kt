package com.dnd.helper.data.import

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository

expect object SessionImporter {
    suspend fun import(repository: CharacterRepository): Result<Unit>
}
