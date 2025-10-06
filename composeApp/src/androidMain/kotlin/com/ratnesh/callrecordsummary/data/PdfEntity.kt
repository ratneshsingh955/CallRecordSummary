package com.ratnesh.callrecordsummary.data

import java.util.Date

/**
 * Data class representing a PDF entity in the database.
 */
data class PdfEntity(
    val id: Long = 0,
    val name: String,
    val data: ByteArray,
    val description: String = "",
    val createdAt: Date = Date(),
    val localFilePath: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (!data.contentEquals(other.data)) return false
        if (description != other.description) return false
        if (createdAt != other.createdAt) return false
        if (localFilePath != other.localFilePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (localFilePath?.hashCode() ?: 0)
        return result
    }
}

