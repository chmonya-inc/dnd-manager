package com.dnd.helper.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import com.dnd.helper.data.config.GeneratedConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.random.Random

@Serializable
data class ImgbbResponse(
    val data: ImgbbData? = null,
    val success: Boolean,
    val status: Int
)

@Serializable
data class ImgbbData(
    val url: String,
    @SerialName("display_url") val displayUrl: String? = null,
    val image: ImgbbImage? = null
)

@Serializable
data class ImgbbImage(
    val url: String
)

enum class GenerationType {
    CHARACTER,
    NPC,
    MONSTER,
    LOCATION,
    BATTLEFIELD,
    SKILL,
    ITEM
}

object PromptGenerator {
    fun getFullPrompt(text: String, type: GenerationType): String {
        val base = when (type) {
            GenerationType.CHARACTER, GenerationType.NPC ->
                "A high-resolution, ultra-detailed professional character portrait for a D&D RPG. A $text character in a realistic oil painting style. Bust shot from chest up. Sharp focus on facial features and textures. Intricate armor or clothing details. Dramatic atmospheric lighting, cinematic composition. Realistic skin textures, fine hair detail. Dark moody background. Flux model, 8k, masterpiece."
            GenerationType.MONSTER ->
                "An epic, wide-angle cinematic dark fantasy illustration capturing an overwhelming sense of cosmic horror and cataclysmic scale. A colossal, titanic $text rises majestically out of a raging, violent turquoise ocean, its massive form towering into the clouds and blotting out the sky. \n" +
                        "\n" +
                        "In the lower foreground, a small, fragile wooden ship is helplessly tossed around by massive crashing waves and churning sea foam, dramatically emphasizing the unfathomable, god-like size of the creature. Sharp, jagged dark rock spires and ancient obsidian monoliths jut out from the turbulent water. The sky is chaotic, filled with rolling dark storm clouds and dense fog, shattered by brilliant flashes of jagged teal and yellow lightning that vividly illuminate the creature's colossal silhouette. Masterpiece digital art, dynamic environmental movement, intense cinematic lighting, atmospheric perspective, grimdark mythic fantasy style."
            GenerationType.LOCATION ->
                "A high-resolution, ultra-detailed wide-angle landscape painting of a $text, a legendary D&D RPG location. Breathtaking scenery, epic scale, intricate architectural details or natural formations. Magical atmosphere, volumetric lighting, god rays. Rich color palette, deep textures. Cinematic composition, sharp focus. 8k resolution, flux model, digital masterpiece."
            GenerationType.BATTLEFIELD ->
                "A top-down 2D tactical fantasy RPG battle map of a $text. High-quality digital painting style, detailed textures, vibrant colors, clear layout with pathways, structures, and tactical elements. Overlayed with a subtle, clean square grid. Bird's-eye view, perfectly flat overhead perspective, ideal for a D&D game."
            GenerationType.SKILL, GenerationType.ITEM ->
                "A high-resolution, ultra-detailed square RPG inventory icon of $text, D&D style. Intricate materials, polished textures, magical energy effects. Dramatic studio lighting, sharp focus on micro-details. Clean dark moody vignette background. Fantasy game asset, octane render style."
        }
        return "$base No text, no letters, no words, no signatures, no watermark. MUST have no text on it."
    }
}

class AiImageService(
    private val httpClient: HttpClient,
    private val storage: com.dnd.helper.domain.storage.CharacterStorage
) {
    private val serverAddress: String get() = storage.getComfyUiAddress() ?: "127.0.0.1:8000"
    private val imgbbApiKey = GeneratedConfig.IMGBB_API_KEY

    private val json = Json { ignoreUnknownKeys = true }

    private val workflowJson = """
    {
      "9": {
        "inputs": {
          "filename_prefix": "Flux2-Klein",
          "images": ["75:65", 0]
        },
        "class_type": "SaveImage"
      },
      "75:61": {
        "inputs": { "sampler_name": "euler" },
        "class_type": "KSamplerSelect"
      },
      "75:62": {
        "inputs": {
          "steps": 20,
          "width": ["75:68", 0],
          "height": ["75:69", 0]
        },
        "class_type": "Flux2Scheduler"
      },
      "75:63": {
        "inputs": {
          "cfg": 5,
          "model": ["75:70", 0],
          "positive": ["75:74", 0],
          "negative": ["75:67", 0]
        },
        "class_type": "CFGGuider"
      },
      "75:64": {
        "inputs": {
          "noise": ["75:73", 0],
          "guider": ["75:63", 0],
          "sampler": ["75:61", 0],
          "sigmas": ["75:62", 0],
          "latent_image": ["75:66", 0]
        },
        "class_type": "SamplerCustomAdvanced"
      },
      "75:65": {
        "inputs": {
          "samples": ["75:64", 0],
          "vae": ["75:72", 0]
        },
        "class_type": "VAEDecode"
      },
      "75:66": {
        "inputs": {
          "width": ["75:68", 0],
          "height": ["75:69", 0],
          "batch_size": 1
        },
        "class_type": "EmptyFlux2LatentImage"
      },
      "75:67": {
        "inputs": {
          "text": "",
          "clip": ["75:71", 0]
        },
        "class_type": "CLIPTextEncode"
      },
      "75:68": {
        "inputs": { "value": 1024 },
        "class_type": "PrimitiveInt"
      },
      "75:69": {
        "inputs": { "value": 1024 },
        "class_type": "PrimitiveInt"
      },
      "75:73": {
        "inputs": { "noise_seed": 1030697849677438 },
        "class_type": "RandomNoise"
      },
      "75:70": {
        "inputs": {
          "unet_name": "flux-2-klein-base-9b-fp8.safetensors",
          "weight_dtype": "default"
        },
        "class_type": "UNETLoader"
      },
      "75:71": {
        "inputs": {
          "clip_name": "qwen_3_8b_fp8mixed.safetensors",
          "type": "flux2",
          "device": "default"
        },
        "class_type": "CLIPLoader"
      },
      "75:72": {
        "inputs": { "vae_name": "full_encoder_small_decoder.safetensors" },
        "class_type": "VAELoader"
      },
      "75:74": {
        "inputs": {
          "text": "",
          "clip": ["75:71", 0]
        },
        "class_type": "CLIPTextEncode"
      }
    }
    """.trimIndent()

    suspend fun generateImage(
        promptText: String, 
        type: GenerationType = GenerationType.CHARACTER,
        customWidth: Int? = null,
        customHeight: Int? = null,
        onRemoteIdGenerated: ((String) -> Unit)? = null
    ): String? {
        val requestClientId = Random.nextLong().toString()
        try {
            val (defaultWidth, defaultHeight) = when (type) {
                GenerationType.CHARACTER, GenerationType.NPC -> Pair(1024, 1024)
                GenerationType.MONSTER -> Pair(1024, 1024)
                GenerationType.LOCATION -> Pair(2048, 2048)
                GenerationType.BATTLEFIELD -> Pair(2048, 2048)
                GenerationType.SKILL, GenerationType.ITEM -> Pair(256, 256)
            }
            
            val width = customWidth ?: defaultWidth
            val height = customHeight ?: defaultHeight

            println("Generating image with prompt: $promptText and size: ${width}x${height}")
            
            val savedWorkflow = storage.getComfyUiWorkflow()
            val workflow = json.parseToJsonElement(savedWorkflow ?: workflowJson).jsonObject.toMutableMap()
            
            // Dynamically find and update nodes
            for (nodeId in workflow.keys) {
                val node = workflow[nodeId]?.jsonObject?.toMutableMap() ?: continue
                val inputs = node["inputs"]?.jsonObject?.toMutableMap() ?: continue
                var updated = false

                // Update Prompt (find CLIPTextEncode or similar with "text" input)
                if (inputs.containsKey("text") && node["class_type"]?.jsonPrimitive?.content?.contains("CLIPText") == true) {
                    val currentText = inputs["text"]?.jsonPrimitive?.content ?: ""
                    // Update if it's the positive prompt (usually the empty one or specifically marked)
                    if (currentText.isEmpty() || currentText == "PROMPT_HERE" || nodeId == "75:74") {
                        inputs["text"] = JsonPrimitive(promptText)
                        updated = true
                    }
                }

                // Update Seed
                if (inputs.containsKey("noise_seed")) {
                    inputs["noise_seed"] = JsonPrimitive(Random.nextLong(0, Long.MAX_VALUE))
                    updated = true
                } else if (inputs.containsKey("seed")) {
                    inputs["seed"] = JsonPrimitive(Random.nextLong(0, Long.MAX_VALUE))
                    updated = true
                }

                // Update Steps
                if (inputs.containsKey("steps")) {
                    inputs["steps"] = JsonPrimitive(storage.getGenerationSteps())
                    updated = true
                }

                // Update Width/Height in Latent Image nodes
                if (inputs.containsKey("width") && inputs.containsKey("height")) {
                    // Only update if they are numbers (not references to other nodes)
                    if (inputs["width"] is JsonPrimitive && inputs["height"] is JsonPrimitive) {
                        inputs["width"] = JsonPrimitive(width)
                        inputs["height"] = JsonPrimitive(height)
                        updated = true
                    }
                }

                // Update Width/Height in PrimitiveInt nodes (common for Flux workflows)
                if (node["class_type"]?.jsonPrimitive?.content == "PrimitiveInt" && inputs.containsKey("value")) {
                    // Try to guess if it's width or height based on current value or ID
                    if (nodeId == "75:68" || nodeId.contains("width", ignoreCase = true)) {
                        inputs["value"] = JsonPrimitive(width)
                        updated = true
                    } else if (nodeId == "75:69" || nodeId.contains("height", ignoreCase = true)) {
                        inputs["value"] = JsonPrimitive(height)
                        updated = true
                    }
                }

                if (updated) {
                    node["inputs"] = JsonObject(inputs)
                    workflow[nodeId] = JsonObject(node)
                }
            }

            // 1. Queue prompt
            val queueResponse: HttpResponse = httpClient.post("http://${serverAddress}/prompt") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("prompt", JsonObject(workflow))
                    put("client_id", requestClientId)
                })
            }
            
            val promptId = json.parseToJsonElement(queueResponse.bodyAsText()).jsonObject["prompt_id"]?.jsonPrimitive?.content ?: return null
            onRemoteIdGenerated?.invoke(promptId)
            
            // 2. Wait for completion via WebSocket
            var completed = false
            httpClient.webSocket("ws://${serverAddress}/ws?clientId=${requestClientId}") {
                while (!completed) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val message = json.parseToJsonElement(frame.readText()).jsonObject
                        if (message["type"]?.jsonPrimitive?.content == "executing") {
                            val data = message["data"]?.jsonObject
                            if (data?.get("node") is JsonNull && data["prompt_id"]?.jsonPrimitive?.content == promptId) {
                                completed = true
                            }
                        }
                    }
                }
            }
            
            // 3. Get history
            val historyResponse: HttpResponse = httpClient.get("http://${serverAddress}/history/${promptId}")
            val history = json.parseToJsonElement(historyResponse.bodyAsText()).jsonObject[promptId]?.jsonObject ?: return null
            val outputs = history["outputs"]?.jsonObject ?: return null
            
            var filename = ""
            var subfolder = ""
            var folderType = ""
            
            for (nodeId in outputs.keys) {
                val nodeOutput = outputs[nodeId]?.jsonObject
                if (nodeOutput?.containsKey("images") == true) {
                    val image = nodeOutput["images"]?.jsonArray?.get(0)?.jsonObject
                    filename = image?.get("filename")?.jsonPrimitive?.content ?: ""
                    subfolder = image?.get("subfolder")?.jsonPrimitive?.content ?: ""
                    folderType = image?.get("type")?.jsonPrimitive?.content ?: ""
                    break
                }
            }
            
            if (filename.isEmpty()) return null
            
            // 4. Download image
            val imageResponse: HttpResponse = httpClient.get("http://${serverAddress}/view") {
                parameter("filename", filename)
                parameter("subfolder", subfolder)
                parameter("type", folderType)
            }
            val imageBytes = imageResponse.readRawBytes()
            
            // 5. Upload to ImgBB
            return uploadToImgbb(imageBytes)
            
        } catch (e: Exception) {
            println("Error generating image: ${e.message}")
            return null
        }
    }

    private suspend fun uploadToImgbb(imageBytes: ByteArray): String? {
        return try {
            val response: HttpResponse = httpClient.submitFormWithBinaryData(
                url = "https://api.imgbb.com/1/upload",
                formData = formData {
                    append("key", imgbbApiKey)
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=\"image.png\"")
                    })
                }
            )
            
            val result = json.decodeFromString<ImgbbResponse>(response.bodyAsText())
            // Try to get the most direct image URL available in the response
            val directUrl = result.data?.image?.url ?: result.data?.displayUrl ?: result.data?.url
            println("[ImgBB] Upload successful. Direct URL: $directUrl")
            directUrl
        } catch (e: Exception) {
            println("Error uploading to ImgBB: ${e.message}")
            null
        }
    }
}
