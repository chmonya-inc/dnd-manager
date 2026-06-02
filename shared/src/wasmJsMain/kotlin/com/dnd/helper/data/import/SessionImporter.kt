package com.dnd.helper.data.import

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository

actual object SessionImporter {
    actual suspend fun import(repository: CharacterRepository): Result<Unit> {
        return Result.Error(com.dnd.helper.domain.common.AppError.Unknown("XLSX Import is only supported on Desktop"))
    }
}
