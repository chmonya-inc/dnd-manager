package com.dnd.helper.domain.utils

object ImageUrlHelper {
    /**
     * Converts a Google Drive sharing link into a direct link that can be loaded by Coil/AsyncImage.
     * Example: https://drive.google.com/file/d/1A2B3C/view -> https://lh3.googleusercontent.com/d/1A2B3C
     */
    fun process(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        // Already a direct link or not a Google Drive link
        if (url.contains("googleusercontent")) return url
        if (!url.contains("drive.google.com")) return url
        
        return try {
            // Pattern 1: .../d/FILE_ID/view...
            if (url.contains("/d/")) {
                val split = url.split("/")
                // Usually the ID is between /d/ and the next slash (or end of string)
                val dIndex = split.indexOf("d")
                if (dIndex != -1 && dIndex + 1 < split.size) {
                    val id = split[dIndex + 1]
                    "https://lh3.googleusercontent.com/d/$id"
                } else {
                    url
                }
            } else if (url.contains("id=")) {
                // Pattern 2: ...?id=FILE_ID
                val id = url.substringAfter("id=").substringBefore("&")
                "https://lh3.googleusercontent.com/d/$id"
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }
}
