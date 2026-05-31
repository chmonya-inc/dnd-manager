package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class PresentedItem(
    val id: String,
    val title: String,
    val type: String,
    val imageUrl: String? = null,
    // x, y, width, height are all in logical units (0 to 1000)
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 200f,
    val height: Float = 200f,
    val isBackground: Boolean = false
)

class PresentationViewModel : ViewModel() {
    private val _isWindowOpen = MutableStateFlow(false)
    val isWindowOpen = _isWindowOpen.asStateFlow()

    val activeItems = mutableStateListOf<PresentedItem>()

    fun toggleWindow() {
        _isWindowOpen.update { !it }
    }

    fun setWindowOpen(open: Boolean) {
        _isWindowOpen.value = open
    }

    fun addItem(title: String, type: String = "Item", imageUrl: String? = null, isBackground: Boolean = false) {
        val id = Random.nextLong().toString()
        if (isBackground) {
            activeItems.removeAll { it.isBackground }
        }
        
        // Backgrounds default to filling the 1000x1000 logical canvas
        val width = if (isBackground) 1000f else 150f
        val height = if (isBackground) 1000f else 180f
        
        activeItems.add(PresentedItem(
            id = id, 
            title = title, 
            type = type, 
            imageUrl = imageUrl,
            isBackground = isBackground, 
            width = width, 
            height = height,
            x = if (isBackground) 0f else 100f,
            y = if (isBackground) 0f else 100f
        ))
    }

    fun updatePosition(id: String, x: Float, y: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                x = x.coerceIn(0f, 1000f), 
                y = y.coerceIn(0f, 1000f)
            )
        }
    }

    fun updateSize(id: String, width: Float, height: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                width = width.coerceIn(10f, 1000f),
                height = height.coerceIn(10f, 1000f)
            )
        }
    }

    fun removeItem(id: String) {
        activeItems.removeAll { it.id == id }
    }

    fun clearItems() {
        activeItems.clear()
    }
}
