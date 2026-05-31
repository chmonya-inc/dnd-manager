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
    // x and y are proportional coordinates (0.0 to 1.0)
    val x: Float = 0.1f,
    val y: Float = 0.1f,
    // width and height are in Dp
    val width: Float = 200f,
    val height: Float = 150f,
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
        
        val width = if (isBackground) 800f else 200f
        val height = if (isBackground) 600f else 240f
        
        activeItems.add(PresentedItem(
            id = id, 
            title = title, 
            type = type, 
            imageUrl = imageUrl,
            isBackground = isBackground, 
            width = width, 
            height = height,
            x = 0f,
            y = 0f
        ))
    }

    fun updatePosition(id: String, x: Float, y: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                x = x.coerceIn(0f, 1f), 
                y = y.coerceIn(0f, 1f)
            )
        }
    }

    fun updateSize(id: String, width: Float, height: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                width = width.coerceAtLeast(50f),
                height = height.coerceAtLeast(50f)
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
