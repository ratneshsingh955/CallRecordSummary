package com.ratnesh.callrecordsummary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ratnesh.callrecordsummary.data.PdfEntity
import com.ratnesh.callrecordsummary.repository.PdfRepository
import com.ratnesh.callrecordsummary.service.CallRecordingsService
import com.ratnesh.callrecordsummary.service.FirebaseAiService
import com.ratnesh.callrecordsummary.utils.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for PDF management operations.
 * This class handles the UI state and business logic for PDF operations.
 */
class PdfViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PdfRepository(application)
    private val firebaseAiService = FirebaseAiService(application)
    private val callRecordingsService = CallRecordingsService(application)

    // UI State
    private val _pdfs = MutableStateFlow<List<PdfEntity>>(emptyList())
    val pdfs: StateFlow<List<PdfEntity>> = _pdfs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    // Audio Summary State
    private val _isProcessingAudioSummary = MutableStateFlow(false)
    val isProcessingAudioSummary: StateFlow<Boolean> = _isProcessingAudioSummary.asStateFlow()

    private val _audioSummaryText = MutableStateFlow("")
    val audioSummaryText: StateFlow<String> = _audioSummaryText.asStateFlow()

    private val _audioSummaryError = MutableStateFlow<String?>(null)
    val audioSummaryError: StateFlow<String?> = _audioSummaryError.asStateFlow()

    // Call Recordings State
    private val _callRecordings = MutableStateFlow<List<CallRecordingsService.CallRecording>>(emptyList())
    val callRecordings: StateFlow<List<CallRecordingsService.CallRecording>> = _callRecordings.asStateFlow()

    private val _isLoadingCallRecordings = MutableStateFlow(false)
    val isLoadingCallRecordings: StateFlow<Boolean> = _isLoadingCallRecordings.asStateFlow()

    private val _callRecordingsError = MutableStateFlow<String?>(null)
    val callRecordingsError: StateFlow<String?> = _callRecordingsError.asStateFlow()

    init {
        loadAllPdfs()
    }

    /**
     * Load all PDFs from the database.
     */
    fun loadAllPdfs() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val pdfList = repository.getAllPdfs()
                _pdfs.value = pdfList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load PDFs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save a PDF to the database.
     *
     * @param name The name of the PDF
     * @param data The PDF file data
     * @param description Optional description
     */
    fun savePdf(name: String, data: ByteArray, description: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _uploadSuccess.value = false

            try {
                val pdfEntity = repository.createPdfEntity(name, data, description)
                pdfEntity?.let {
                    repository.savePdf(it)
                    _uploadSuccess.value = true
                    loadAllPdfs()
                }
                if (pdfEntity == null) {
                    _errorMessage.value = "Failed to save PDF"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to save PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a PDF from the database.
     *
     * @param id The ID of the PDF to delete
     */
    fun deletePdf(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.deletePdf(id)
                loadAllPdfs() // Refresh the list
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search PDFs by name.
     *
     * @param query The search query
     */
    fun searchPdfs(query: String) {
        if (query.isEmpty()) {
            loadAllPdfs()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val searchResults = repository.searchPdfsByName(query)
                _pdfs.value = searchResults
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search PDFs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear upload success state.
     */
    fun clearUploadSuccess() {
        _uploadSuccess.value = false
    }

    /**
     * Get a PDF by ID.
     *
     * @param id The ID of the PDF
     * @return The PDF entity or null if not found
     */
    suspend fun getPdfById(id: Long): PdfEntity? {
        return repository.getPdfById(id)
    }

    /**
     * Get the original file from local storage.
     *
     * @param pdfEntity The PDF entity containing the local file path
     * @return The original file if it exists, null otherwise
     */
    fun getOriginalFile(pdfEntity: PdfEntity): java.io.File? {
        return repository.getOriginalFile(pdfEntity)
    }

    /**
     * Check if the original file exists in local storage.
     *
     * @param pdfEntity The PDF entity containing the local file path
     * @return True if the file exists, false otherwise
     */
    fun hasOriginalFile(pdfEntity: PdfEntity): Boolean {
        return repository.hasOriginalFile(pdfEntity)
    }

    /**
     * Get the size of the original file in local storage.
     *
     * @param pdfEntity The PDF entity containing the local file path
     * @return The file size in bytes, or -1 if the file doesn't exist
     */
    fun getOriginalFileSize(pdfEntity: PdfEntity): Long {
        return repository.getOriginalFileSize(pdfEntity)
    }

    /**
     * Clear all data from the database.
     * This is useful when schema changes require a fresh start.
     * WARNING: This will delete all data permanently.
     */
    fun clearAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.clearAllData()
                _pdfs.value = emptyList()
                _uploadSuccess.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check if the database is empty.
     *
     * @return True if no data exists, false otherwise
     */
    fun isDatabaseEmpty(): Boolean {
        return repository.isDatabaseEmpty()
    }

    /**
     * Process audio file for summary generation.
     * 
     * @param fileName The name of the audio file
     * @param audioData The audio file data as ByteArray
     * @param language The language for summary generation ("English" or "Hindi")
     */
    fun processAudioFileForSummary(fileName: String, audioData: ByteArray, language: String = "English") {
        viewModelScope.launch {
            _isProcessingAudioSummary.value = true
            _audioSummaryError.value = null
            _audioSummaryText.value = ""

            try {
                Log.d("PdfViewModel", "🎵 Starting audio summary processing: $fileName")
                Log.d("PdfViewModel", "📊 Audio file size: ${audioData.size} bytes")

                // First, extract text from audio using existing method
                val extractedText = repository.extractTextFromAudio(fileName, audioData)
                Log.d("PdfViewModel", "📝 Extracted text length: ${extractedText.length}")

                if (extractedText.isNotEmpty()) {
                    // Generate AI summary from the extracted text with selected language
                    val summary = firebaseAiService.generateSummaryFromText(extractedText, language)
                    Log.d("PdfViewModel", "✅ Audio summary generated successfully in $language")
                    _audioSummaryText.value = summary
                } else {
                    Log.w("PdfViewModel", "⚠️ No text extracted from audio file")
                    _audioSummaryError.value = "Could not extract text from the audio file. Please try a different audio file."
                }

            } catch (e: Exception) {
                Log.e("PdfViewModel", "❌ Error processing audio file for summary", e)
                _audioSummaryError.value = "Failed to process audio file: ${e.message}"
            } finally {
                _isProcessingAudioSummary.value = false
            }
        }
    }

    /**
     * Clear audio summary data.
     */
    fun clearAudioSummaryData() {
        _audioSummaryText.value = ""
        _audioSummaryError.value = null
        _isProcessingAudioSummary.value = false
    }

    /**
     * Fetch call recordings from the device.
     */
    fun fetchCallRecordings() {
        Log.d("PdfViewModel", "🚀 fetchCallRecordings() called")
        viewModelScope.launch {
            Log.d("PdfViewModel", "🔄 Starting coroutine for fetchCallRecordings")
            _isLoadingCallRecordings.value = true
            _callRecordingsError.value = null
            _callRecordings.value = emptyList()

            try {
                Log.d("PdfViewModel", "📞 Fetching call recordings...")

                val recordings = callRecordingsService.fetchCallRecordings()
                _callRecordings.value = recordings
                Log.d("PdfViewModel", "✅ Found ${recordings.size} call recordings")
                Log.d("PdfViewModel", "📊 Recordings: ${recordings.map { it.displayName ?: it.contactName ?: "Unknown" }}")
            } catch (e: Exception) {
                Log.e("PdfViewModel", "❌ Error fetching call recordings", e)
                _callRecordingsError.value = "Failed to fetch call recordings: ${e.message}"
            } finally {
                _isLoadingCallRecordings.value = false
                Log.d("PdfViewModel", "🏁 fetchCallRecordings completed")
            }
        }
    }

    /**
     * Process a call recording for summary generation.
     * 
     * @param recording The call recording to process
     * @param language The language for summary generation
     */
    fun processCallRecording(recording: CallRecordingsService.CallRecording, language: String) {
        viewModelScope.launch {
            _isProcessingAudioSummary.value = true
            _audioSummaryError.value = null
            _audioSummaryText.value = ""

            try {
                Log.d("PdfViewModel", "📞 Processing call recording: ${recording.contactName ?: recording.phoneNumber}")
                
                val file = File(recording.filePath ?: "")
                if (!file.exists()) {
                    _audioSummaryError.value = "Recording file not found. It may have been deleted."
                    return@launch
                }

                val audioData = file.readBytes()
                val fileName = file.name

                // Extract text from audio
                val extractedText = repository.extractTextFromAudio(fileName, audioData)
                Log.d("PdfViewModel", "📝 Extracted text length: ${extractedText.length}")

                if (extractedText.isNotEmpty()) {
                    // Generate AI summary from the extracted text with selected language
                    val summary = firebaseAiService.generateSummaryFromText(extractedText, language)
                    Log.d("PdfViewModel", "✅ Call recording summary generated successfully in $language")
                    _audioSummaryText.value = summary
                } else {
                    Log.w("PdfViewModel", "⚠️ No text extracted from call recording")
                    _audioSummaryError.value = "Could not extract text from the call recording. The audio may be corrupted or in an unsupported format."
                }

            } catch (e: Exception) {
                Log.e("PdfViewModel", "❌ Error processing call recording", e)
                _audioSummaryError.value = "Failed to process call recording: ${e.message}"
            } finally {
                _isProcessingAudioSummary.value = false
            }
        }
    }

    /**
     * Clear call recordings data.
     */
    fun clearCallRecordingsData() {
        _callRecordings.value = emptyList()
        _callRecordingsError.value = null
        _isLoadingCallRecordings.value = false
    }

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
        Log.d("PdfViewModel", "ViewModel cleared - all resources cleaned up")
    }
}

