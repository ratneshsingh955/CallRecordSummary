package com.ratnesh.callrecordsummary.repository

import android.content.Context
import android.util.Log
import com.ratnesh.callrecordsummary.data.PdfEntity
import com.ratnesh.callrecordsummary.service.MlKitTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * Repository for managing PDF data and AI processing.
 */
class PdfRepository(private val context: Context) {

    companion object {
        private const val TAG = "PdfRepository"
    }

    private val textExtractor = MlKitTextExtractor(context)
    private val pdfs = mutableListOf<PdfEntity>()

    /**
     * Get all PDFs from the repository.
     */
    suspend fun getAllPdfs(): List<PdfEntity> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting all PDFs, count: ${pdfs.size}")
        return@withContext pdfs.toList()
    }

    /**
     * Save a PDF to the repository.
     */
    suspend fun savePdf(pdf: PdfEntity) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Saving PDF: ${pdf.name}")
        pdfs.add(pdf)
    }

    /**
     * Delete a PDF from the repository.
     */
    suspend fun deletePdf(id: Long) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Deleting PDF with id: $id")
        pdfs.removeAll { it.id == id }
    }

    /**
     * Search PDFs by name.
     */
    suspend fun searchPdfsByName(query: String): List<PdfEntity> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Searching PDFs with query: $query")
        return@withContext pdfs.filter { it.name.contains(query, ignoreCase = true) }
    }

    /**
     * Get a PDF by ID.
     */
    suspend fun getPdfById(id: Long): PdfEntity? = withContext(Dispatchers.IO) {
        return@withContext pdfs.find { it.id == id }
    }

    /**
     * Create a PDF entity from file data.
     */
    suspend fun createPdfEntity(name: String, data: ByteArray, description: String = ""): PdfEntity? = withContext(Dispatchers.IO) {
        try {
            val id = System.currentTimeMillis() // Simple ID generation
            return@withContext PdfEntity(
                id = id,
                name = name,
                data = data,
                description = description
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PDF entity", e)
            return@withContext null
        }
    }

    /**
     * Extract text from audio file using AI.
     */
    suspend fun extractTextFromAudio(fileName: String, audioData: ByteArray): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Extracting text from audio: $fileName")
        try {
            val extractedText = textExtractor.extractTextFromFile(fileName, audioData)
            Log.d(TAG, "Audio text extraction completed, length: ${extractedText.length}")
            return@withContext extractedText
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from audio", e)
            return@withContext ""
        }
    }

    /**
     * Get the original file from local storage.
     */
    fun getOriginalFile(pdfEntity: PdfEntity): File? {
        return pdfEntity.localFilePath?.let { File(it) }
    }

    /**
     * Check if the original file exists in local storage.
     */
    fun hasOriginalFile(pdfEntity: PdfEntity): Boolean {
        return pdfEntity.localFilePath?.let { File(it).exists() } ?: false
    }

    /**
     * Get the size of the original file in local storage.
     */
    fun getOriginalFileSize(pdfEntity: PdfEntity): Long {
        return pdfEntity.localFilePath?.let { File(it).length() } ?: -1L
    }

    /**
     * Clear all data from the repository.
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Clearing all data")
        pdfs.clear()
    }

    /**
     * Check if the repository is empty.
     */
    fun isDatabaseEmpty(): Boolean {
        return pdfs.isEmpty()
    }

    /**
     * Clean up resources.
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up repository resources")
        textExtractor.cleanup()
    }
}

