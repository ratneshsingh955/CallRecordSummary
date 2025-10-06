package com.ratnesh.callrecordsummary.service

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firebase AI Generative Model service for generating detailed content from files.
 * Uses Google's Gemini model to analyze file content and generate comprehensive descriptions.
 */
class FirebaseAiService(private val context: Context) {

    companion object {
        private const val TAG = "MLKitOutput" // Single tag for AI output logging
        private const val VECTOR_DEBUG_TAG = "VECTOR_DEBUG" // For internal debugging
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash"
        )
    }

    init {
        Log.d(VECTOR_DEBUG_TAG, "🤖 FirebaseAiService: Initializing Firebase AI service")
        // Initialize anonymous authentication
        initializeAuth()
    }

    /**
     * Initialize Firebase Authentication with anonymous sign-in.
     */
    private fun initializeAuth() {
        Log.d(VECTOR_DEBUG_TAG, "🔐 FirebaseAiService: Initializing Firebase Authentication")

        // Check if user is already signed in
        if (auth.currentUser != null) {
            Log.d(
                VECTOR_DEBUG_TAG,
                "✅ FirebaseAiService: User already authenticated: ${auth.currentUser?.uid}"
            )
            return
        }

        // Sign in anonymously
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        VECTOR_DEBUG_TAG,
                        "✅ FirebaseAiService: Anonymous authentication successful"
                    )
                    Log.d(
                        VECTOR_DEBUG_TAG,
                        "👤 FirebaseAiService: User ID: ${auth.currentUser?.uid}"
                    )
                } else {
                    Log.e(
                        VECTOR_DEBUG_TAG,
                        "❌ FirebaseAiService: Anonymous authentication failed",
                        task.exception
                    )
                }
            }
    }

    /**
     * Ensure user is authenticated before making AI requests.
     */
    private suspend fun ensureAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (auth.currentUser == null) {
                Log.d(
                    VECTOR_DEBUG_TAG,
                    "🔐 FirebaseAiService: No authenticated user, signing in anonymously"
                )
                auth.signInAnonymously().await()
                Log.d(VECTOR_DEBUG_TAG, "✅ FirebaseAiService: Anonymous authentication completed")
            }
            true
        } catch (e: Exception) {
            Log.e(VECTOR_DEBUG_TAG, "❌ FirebaseAiService: Authentication failed", e)
            false
        }
    }

    /**
     * Generate detailed content from file data using Firebase AI.
     *
     * @param fileName Name of the file
     * @param fileData File data as byte array
     * @param fileType Type of file (PDF, IMAGE, etc.)
     * @return Generated detailed content description
     */
    suspend fun generateContentFromFile(
        fileName: String,
        fileData: ByteArray,
        fileType: FileType
    ): String = withContext(Dispatchers.IO) {
        Log.d(VECTOR_DEBUG_TAG, "🤖 FirebaseAiService: Starting AI content generation")
        Log.d(
            VECTOR_DEBUG_TAG,
            "📄 FirebaseAiService: File: $fileName, Type: $fileType, Size: ${fileData.size} bytes"
        )

        try {
            // Ensure user is authenticated
            if (!ensureAuthenticated()) {
                Log.w(
                    VECTOR_DEBUG_TAG,
                    "⚠️ FirebaseAiService: Authentication failed, using fallback"
                )
                val fallbackContent = generateFallbackContent(fileName, fileData, fileType)
                Log.d(TAG, fallbackContent)
                return@withContext fallbackContent
            }

            val prompt = createPrompt(fileName, fileData, fileType)
            Log.d(VECTOR_DEBUG_TAG, "📝 FirebaseAiService: Generated prompt for AI")

            val response = generativeModel.generateContent(
                content {
                    val mimeType = getMimeTypeForFile(fileName)
                    inlineData(fileData, mimeType)
                    text(prompt)
                }
            )
            val generatedContent = response.text ?: ""

            Log.d(VECTOR_DEBUG_TAG, "✅ FirebaseAiService: AI content generation completed")
            Log.d(
                VECTOR_DEBUG_TAG,
                "📊 FirebaseAiService: Generated content length: ${generatedContent.length}"
            )

            // Log the generated content using the required tag
            Log.d(TAG, generatedContent)

            return@withContext generatedContent
        } catch (e: Exception) {
            Log.e(
                VECTOR_DEBUG_TAG,
                "❌ FirebaseAiService: Error generating content with Firebase AI",
                e
            )

            // Fallback to basic content generation if AI fails
            val fallbackContent = generateFallbackContent(fileName, fileData, fileType)
            Log.d(VECTOR_DEBUG_TAG, "🔄 FirebaseAiService: Using fallback content generation")
            Log.d(TAG, fallbackContent)

            return@withContext fallbackContent
        }
    }

    private fun getMimeTypeForFile(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"

            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            "odt" -> "application/vnd.oasis.opendocument.text"

            // Spreadsheets
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"

            // Presentations
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "odp" -> "application/vnd.oasis.opendocument.presentation"

            // Archives
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"

            // Audio (includes call recording formats)
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "aiff" -> "audio/aiff"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "amr" -> "audio/amr"
            "3gp" -> "audio/3gpp"
            "m4a" -> "audio/mp4"

            // Video
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"

            else -> "application/octet-stream"
        }
    }

    /**
     * Create a comprehensive prompt for the AI model based on file type and content.
     */
    private fun createPrompt(fileName: String, fileData: ByteArray, fileType: FileType): String {
        return   createAudioPrompt()
    }

    private fun createAudioPrompt(): String {
        return """
            You are an AI audio analysis and summarization system.  
            Your task is to process the given audio transcript and decide how to respond based on the content type.  
 
            1. **Audio Type Detection**  
            - If the audio is **generic voice content** (e.g., casual speech, music, short generic sentences, background noise, unrelated chatter, or single unrelated phrases), then:  
            → Do not generate a meeting summary.  
            → Instead, provide a **simple and concise generic response** describing the content.  
 
            - If the audio is a **meeting/discussion/conversational context** (two or more people talking, or a single speaker presenting/explaining a topic in detail, such as business, planning, brainstorming, or structured context), then:  
            → Identify it as a "Meeting/Contextual Discussion".  
            → Generate a **clear, structured summary** covering:  
            - Main topics discussed  
            - Key points raised by speakers  
            - Decisions or outcomes (if any)  
            - Action items or follow-ups (if mentioned)  
 
            2. **Output Rules**  
            - Always first state the **detected audio type**: "Generic Audio" or "Meeting/Discussion".  
            - Then provide the appropriate response (generic description or structured summary).  
            - Keep responses concise but informative.  
 
            3. **Tone**  
            - Neutral, professional, and easy to understand.  
            - Do not invent details not supported by the audio transcript.  
        """.trimIndent()
    }



    /**
     * Generate fallback content when AI service is unavailable.
     */
    private fun generateFallbackContent(
        fileName: String,
        fileData: ByteArray,
        fileType: FileType
    ): String {
        val fileSize = fileData.size
        val fileHash = fileData.contentHashCode()

        return """
            FILE ANALYSIS REPORT
            ===================
            
            File Details:
            - Name: $fileName
            - Type: ${fileType.name}
            - Size: ${fileSize} bytes
            - Hash: ${fileHash.toString(16)}
            
            Content Analysis:
            This file appears to be a ${fileType.name.lowercase()} document with a size of ${fileSize} bytes. 
            Based on its characteristics, this file likely contains structured information and data.
            
            Document Structure:
            The file size suggests it contains substantial content. For a ${fileType.name} file of this size, 
            we can expect it to include detailed information, possibly with multiple sections or pages.
            
            Key Characteristics:
            - File size indicates moderate to substantial content
            - ${fileType.name} format suggests professional or structured data
            - Suitable for document processing and analysis
            
            Technical Specifications:
            - Format: ${fileType.name}
            - Size Category: ${getSizeCategory(fileSize)}
            - Processing Complexity: ${getComplexityLevel(fileSize)}
            
            Usage Recommendations:
            This file is suitable for:
            - Document analysis and processing
            - Content extraction and indexing
            - Vector database storage
            - Similarity search operations
            
            Note: This analysis was generated using fallback methods due to AI service unavailability.
            For more detailed analysis, ensure Firebase AI service is properly configured.
        """.trimIndent()
    }

    /**
     * Get size category based on file size.
     */
    private fun getSizeCategory(size: Int): String {
        return when {
            size < 10_000 -> "Small"
            size < 100_000 -> "Medium"
            size < 1_000_000 -> "Large"
            else -> "Very Large"
        }
    }

    /**
     * Get complexity level based on file size.
     */
    private fun getComplexityLevel(size: Int): String {
        return when {
            size < 50_000 -> "Simple"
            size < 200_000 -> "Moderate"
            size < 500_000 -> "Complex"
            else -> "Highly Complex"
        }
    }

    /**
     * Generate a summary from text using Firebase AI.
     * This function takes any text content and generates a comprehensive summary.
     *
     * @param text The text content to summarize
     * @param language The language for summary generation ("English" or "Hindi")
     * @return AI-generated summary of the text
     */
    suspend fun generateSummaryFromText(text: String, language: String = "English"): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "📝 FirebaseAiService: Generating summary from text")
        Log.d(TAG, "📊 FirebaseAiService: Text length: ${text.length} characters")

        try {
            // Ensure user is authenticated
            if (!ensureAuthenticated()) {
                Log.w(TAG, "⚠️ FirebaseAiService: Authentication failed, using fallback")
                return@withContext generateTextSummaryFallback(text)
            }

            val prompt = if (language.lowercase() == "hindi") {
                buildHindiSummaryPrompt(text)
            } else {
                buildTextSummaryPrompt(text)
            }
            Log.d(TAG, "📋 FirebaseAiService: Generated text summary prompt")

            val response = generativeModel.generateContent(prompt)
            val summary = response.text ?: generateTextSummaryFallback(text)

            Log.d(TAG, "✅ FirebaseAiService: Text summary generated successfully")
            Log.d(TAG, "📊 FirebaseAiService: Summary length: ${summary.length} characters")

            return@withContext summary

        } catch (e: Exception) {
            Log.e(TAG, "❌ FirebaseAiService: Error generating text summary", e)
            return@withContext generateTextSummaryFallback(text)
        }
    }

    /**
     * Build a comprehensive prompt for summarizing general text content.
     *
     * @param text The text content to summarize
     * @return Formatted prompt for the AI model
     */
    private fun buildTextSummaryPrompt(text: String): String {
        return """
    You are an assistant that writes **clear, natural, and easy-to-read summaries** of any text.
    Your goal is to help someone quickly understand what the text is about —
    as if you were explaining it to a friend who didn't read it.

    TEXT TO SUMMARIZE:
    $text

    YOUR TASK:
    1. Write a **simple, natural summary** that covers the main ideas, important details, and any useful insights.
    2. Use a **friendly, human tone** — not robotic or academic.
    3. Keep the summary **concise but complete** — enough for someone to fully understand the content.
    4. If the text is a conversation or call, describe what was discussed and what was decided.
    5. If it's a document, article, or notes, summarize the key sections and main message.
    6. Keep formatting light and easy to scan.

    FORMAT:
    ## Summary

    ### Main Idea
    - A short overview of what this text is mainly about.

    ### Important Points
    - A few bullet points summarizing the key details, topics, or decisions.

    ### Extra Notes
    - Any useful insights, context, or follow-ups worth mentioning.

    STYLE GUIDE:
    - Use **plain, natural language**.
    - Avoid overusing markdown or bold unless needed.
    - Don't make it sound like an AI wrote it.
    - Focus on *clarity and readability*.
    - If the text includes emotions, tone, or sentiment, capture it briefly in your own words.

    GOAL:
    Make the summary sound like it was written by a helpful human who read and understood the content carefully.
    """.trimIndent()
    }

    private fun buildHindiSummaryPrompt(text: String): String {
        return """
    आप एक सहायक हैं जो किसी भी टेक्स्ट या बातचीत का **स्पष्ट, आसान और स्वाभाविक हिंदी सारांश** लिखते हैं।
    आपका उद्देश्य यह है कि कोई व्यक्ति जल्दी से समझ सके कि इस टेक्स्ट या बातचीत में क्या कहा गया है,
    जैसे आप किसी दोस्त को समझा रहे हों जिसने इसे नहीं पढ़ा या सुना हो।

    सारांश बनाने के लिए टेक्स्ट:
    $text

    आपका कार्य:
    1. एक **सादा और स्वाभाविक हिंदी सारांश** लिखें जिसमें मुख्य बातें और ज़रूरी विवरण हों।
    2. अगर टेक्स्ट में **कई स्पीकर्स** (जैसे कॉल या मीटिंग) हैं:
       - हर स्पीकर का नाम या रोल पहचानें (जैसे "राहुल:", "एजेंट:", "ग्राहक:")
       - हर स्पीकर ने क्या कहा उसका सारांश अपने शब्दों में लिखें।
       - मुख्य चर्चा, सवाल और निर्णयों को शामिल करें।
    3. अगर टेक्स्ट बातचीत नहीं है (जैसे कोई आर्टिकल या नोट्स), तो सामान्य सारांश लिखें।
    4. भाषा को **दोस्ताना, सरल और प्राकृतिक** रखें — मशीन जैसी नहीं।
    5. सारांश को **संक्षिप्त लेकिन पूरा** बनाएं।

    प्रारूप:
    ## सारांश

    ### मुख्य विषय
    - यह टेक्स्ट या बातचीत मुख्य रूप से किस बारे में है।

    ### ज़रूरी बिंदु
    - मुख्य बातें, विषय या कार्य जिन पर चर्चा हुई।

    ### स्पीकर्स (अगर हों)
    - **स्पीकर 1 (नाम या भूमिका):** क्या कहा।
    - **स्पीकर 2 (नाम या भूमिका):** क्या जवाब दिया या राय दी।

    ### नोट्स / निष्कर्ष
    - महत्वपूर्ण नतीजे, सुझाव या अगला कदम।
    - अगर बातचीत में कोई भावना या टोन है (जैसे सौहार्दपूर्ण, ग़ुस्से वाला, औपचारिक), तो उसका संक्षिप्त ज़िक्र करें।

    लेखन शैली:
    - आसान और रोज़मर्रा की हिंदी का उपयोग करें।
    - बहुत अधिक मार्कडाउन या तकनीकी फॉर्मेटिंग न करें।
    - सिर्फ़ वही जानकारी शामिल करें जो ज़रूरी और स्पष्ट हो।

    उद्देश्य:
    एक ऐसा सारांश बनाना जो ऐसा लगे जैसे किसी इंसान ने ध्यान से पढ़कर या सुनकर लिखा हो —
    न कि किसी मशीन ने जनरेट किया हो।
    """.trimIndent()
    }

    /**
     * Generate a fallback summary when AI service is unavailable for general text.
     */
    private fun generateTextSummaryFallback(text: String): String {
        val wordCount = text.split("\\s+".toRegex()).size
        val charCount = text.length
        val lines = text.split("\n").size
        
        return """
        ## 📝 Content Summary
        
        ### 📊 Content Statistics
        - **Word Count:** $wordCount words
        - **Character Count:** $charCount characters  
        - **Lines:** $lines lines
        
        ### 📋 Content Overview
        This appears to be text content with $wordCount words, suggesting it contains substantial information or discussion.
        
        ### 🔍 Key Characteristics
        - **Content Type:** Text document or transcript
        - **Length:** ${if (wordCount > 1000) "Long-form content" else if (wordCount > 500) "Medium-length content" else "Short content"}
        - **Language:** ${if (text.any { it.code in 0x0900..0x097F }) "Contains Hindi/Devanagari script" else "English or other language"}
        
        ### 📄 Content Preview
        ${text.take(500)}${if (text.length > 500) "..." else ""}
        
        ### ⚠️ Note
        This is a basic analysis generated without AI processing. For a detailed summary, ensure Firebase AI service is properly configured.
        
        **Full Content Length:** ${text.length} characters
        """.trimIndent()
    }

    /**
     * Enum for supported file types.
     */
    enum class FileType {
        PDF,
        IMAGE,
        DOCUMENT,
        UNSUPPORTED,
        AUDIO
    }
}
