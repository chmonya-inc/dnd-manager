package com.dnd.helper.domain.utils

object ImageUrlHelper {
    /**
     * Converts a Google Drive sharing link into a direct link that can be loaded by Coil/AsyncImage.
     * Use the 'uc' export format which is more reliable for direct downloads.
     */
    fun process(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        // Already a direct link or not a Google Drive link
        if (url.contains("googleusercontent")) return url
        
        // Handle ImgBB viewer links by trying to guess the direct link or at least identifying them
        // Note: ImgBB direct links usually start with i.ibb.co
        if (url.contains("ibb.co") && !url.contains("i.ibb.co")) {
            // This is a viewer link, we should have saved the direct 'i.ibb.co' link instead.
            // Log it so we can identify if this is still happening.
            println("[ImageUrlHelper] Warning: Detected ImgBB viewer link instead of direct image link: $url")
        }

        if (!url.contains("drive.google.com")) return url
        
        return try {
            val id = when {
                // Pattern 1: .../file/d/FILE_ID/view...
                url.contains("/d/") -> {
                    url.substringAfter("/d/").substringBefore("/")
                }
                // Pattern 2: ...?id=FILE_ID (e.g., open?id=..., uc?id=...)
                url.contains("id=") -> {
                    url.substringAfter("id=").substringBefore("&").substringBefore("/").substringBefore(" ")
                }
                else -> null
            }

            if (id != null) {
                // The 'uc?export=download' format is the most reliable way to 
                // get the raw bytes of a file from a public Google Drive.
                "https://drive.google.com/uc?export=download&id=$id"
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }
}
