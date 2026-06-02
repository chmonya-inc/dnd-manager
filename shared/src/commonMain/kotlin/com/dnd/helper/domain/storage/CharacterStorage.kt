package com.dnd.helper.domain.storage

interface CharacterStorage {
    fun saveCharacterId(id: String)
    fun getCharacterId(): String?
    fun saveTableId(id: String)
    fun getTableId(): String?
    fun saveSessions(sessionsJson: String)
    fun getSessions(): String?
    fun saveTheme(themeName: String)
    fun getTheme(): String?
    fun saveComfyUiAddress(address: String)
    fun getComfyUiAddress(): String?
    fun saveComfyUiWorkflow(json: String)
    fun getComfyUiWorkflow(): String?
    fun saveGenerationSteps(steps: Int)
    fun getGenerationSteps(): Int
}
