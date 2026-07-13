package com.dnd.helper.fakes

import com.dnd.helper.data.remote.GenerationType

/**
 * Fake AI image generation service for repository tests. Simulates the behavior
 * of AiImageService without requiring real HTTP calls.
 */
class FakeAiImageService {

    var generateImageResult: String? = "https://example.com/generated.png"
    var generateImageDelay: Long = 50L
    var shouldThrow: Boolean = false

    var generateImageCalls = 0
        private set

    var onRemoteIdGeneratedCalls: MutableList<String> = mutableListOf()

    suspend fun generateImage(
        promptText: String,
        type: GenerationType = GenerationType.CHARACTER,
        customWidth: Int? = null,
        customHeight: Int? = null,
        onRemoteIdGenerated: ((String) -> Unit)? = null
    ): String? {
        generateImageCalls++

        if (shouldThrow) {
            throw Exception("AI service failure")
        }

        // Simulate remote ID callback
        onRemoteIdGenerated?.invoke("remote-id-$generateImageCalls")
        onRemoteIdGenerated?.let { onRemoteIdGeneratedCalls.add("remote-id-$generateImageCalls") }

        // Simulate generation delay
        kotlinx.coroutines.delay(generateImageDelay)

        return generateImageResult
    }

    fun reset() {
        generateImageResult = "https://example.com/generated.png"
        generateImageDelay = 50L
        shouldThrow = false
        generateImageCalls = 0
        onRemoteIdGeneratedCalls.clear()
    }
}
