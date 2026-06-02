package com.dnd.helper.data.import

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import java.awt.FileDialog
import java.awt.Frame

actual object SessionImporter {
    actual suspend fun import(repository: CharacterRepository): Result<Unit> {
        val path = pickXlsxFile() ?: return Result.Success(Unit) // Cancelled
        return XlsxImporter(repository).import(path)
    }

    private fun pickXlsxFile(): String? {
        val dialog = FileDialog(null as Frame?, "Select XLSX Session Export", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> name.endsWith(".xlsx") }
        dialog.isVisible = true
        return if (dialog.file != null) dialog.directory + dialog.file else null
    }
}
