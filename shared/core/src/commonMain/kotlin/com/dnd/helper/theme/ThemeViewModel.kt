package com.dnd.helper.theme

import androidx.lifecycle.ViewModel
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val storage: CharacterStorage
) : ViewModel() {
    private val _currentTheme = MutableStateFlow(loadInitialTheme())
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private fun loadInitialTheme(): AppTheme {
        val saved = storage.getTheme()
        return AppTheme.entries.find { it.name == saved } ?: AppTheme.DUNGEON
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        storage.saveTheme(theme.name)
    }
}
