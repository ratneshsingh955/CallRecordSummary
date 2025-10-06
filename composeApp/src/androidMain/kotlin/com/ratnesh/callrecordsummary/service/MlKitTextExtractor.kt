package com.ratnesh.callrecordsummary.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI-powered content generation service for any type of file.
 * Uses Firebase AI Generative Model to analyze files and generate detailed content descriptions.
 */
class MlKitTextExtractor(private val context: Context) {

    companion object {
        private const val TAG = "MLKitOutput" // Single tag for AI output logging
        private const val VECTOR_DEBUG_TAG = "VECTOR_DEBUG" // For internal debugging

        // Supported file extensions
        private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a", "aac", "amr", "3gp", "flac")
    }

    private val firebaseAiService = FirebaseAiService(context)

    /**
     * Generate detailed content from any file using Firebase AI.
     * Uses AI to analyze file characteristics and generate comprehensive content descriptions.
     *
     * @param fileName The name of the file (used for type detection)
     * @param fileData The file data as byte array
     * @return Generated detailed content or empty string if generation fails
     */
    suspend fun extractTextFromFile(fileName: String, fileData: ByteArray): String =
        withContext(Dispatchers.IO) {
            Log.d(VECTOR_DEBUG_TAG, "🤖 MlKitTextExtractor: Starting AI content generation")
            Log.d(
                VECTOR_DEBUG_TAG,
                "📄 MlKitTextExtractor: File: $fileName, Size: ${fileData.size} bytes"
            )

            try {
                val fileType = detectFileType(fileName)
                Log.d(VECTOR_DEBUG_TAG, "📊 MlKitTextExtractor: Detected file type: $fileType")

                val generatedContent = firebaseAiService.generateContentFromFile(fileName, fileData, FirebaseAiService.FileType.AUDIO)

                Log.d(VECTOR_DEBUG_TAG, "✅ MlKitTextExtractor: AI content generation completed")
                Log.d(
                    VECTOR_DEBUG_TAG,
                    "📊 MlKitTextExtractor: Generated content length: ${generatedContent.length}"
                )

                // Log the generated content using the required tag
                Log.d(TAG, generatedContent)

                return@withContext generatedContent
            } catch (e: Exception) {
                Log.e(VECTOR_DEBUG_TAG, "❌ MlKitTextExtractor: Error generating content", e)
                Log.d(TAG, "") // Log empty string for consistency
                return@withContext ""
            }
        }

    /**
     * Detect file type based on file extension.
     *
     * @param fileName The file name
     * @return Detected file type
     */
    private fun detectFileType(fileName: String): FileType {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        
        Log.d(VECTOR_DEBUG_TAG, "🔍 MlKitTextExtractor: Detecting file type for $fileName")
        Log.d(VECTOR_DEBUG_TAG, "🔍 MlKitTextExtractor: File extension: $extension")
        Log.d(VECTOR_DEBUG_TAG, "🔍 MlKitTextExtractor: Supported audio extensions: $AUDIO_EXTENSIONS")

        val fileType = when {
            extension in AUDIO_EXTENSIONS -> FileType.AUDIO
            else -> FileType.UNSUPPORTED
        }
        
        Log.d(VECTOR_DEBUG_TAG, "🔍 MlKitTextExtractor: Detected file type: $fileType")
        return fileType
    }

    /**
     * Clean up resources.
     */
    fun cleanup() {
        Log.d(VECTOR_DEBUG_TAG, "🧹 MlKitTextExtractor: Cleaning up Firebase AI resources")
        // Firebase AI service doesn't require explicit cleanup
    }

    /**
     * Enum for supported file types.
     */
    private enum class FileType {
        UNSUPPORTED,
        AUDIO
    }
}
