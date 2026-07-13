package com.dnd.helper.data.import

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window

actual object SessionImporter {
    actual suspend fun import(repository: CharacterRepository): Result<Unit> {
        val path = withContext(Dispatchers.Main) {
            pickXlsxFile()
        } ?: return Result.Success(Unit) // Cancelled
        return XlsxImporter(repository).import(path)
    }

    private fun pickXlsxFile(): String? {
        val activeWindow = Window.getWindows()
            .filterIsInstance<Frame>()
            .find { it.isVisible && it.title == "D&D Helper" }
            ?: Window.getWindows().find { it.isVisible && it is Frame } as? Frame

        val dialog = FileDialog(activeWindow, "Select XLSX Session Export", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> name.endsWith(".xlsx") }

        // Force the dialog to be on top of everything
        dialog.isAlwaysOnTop = true

        dialog.isVisible = true
        return if (dialog.file != null) dialog.directory + dialog.file else null
    }
}
