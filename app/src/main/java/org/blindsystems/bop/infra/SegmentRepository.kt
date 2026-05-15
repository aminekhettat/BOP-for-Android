package org.blindsystems.bop.infra

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.blindsystems.bop.core.Segment
import java.io.File

/**
 * Save/load segments to a JSON file named <audioFileName>.segments.json.
 * Mirrors infra/persistence.py.
 */
open class SegmentRepository(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Serializable
    private data class SegmentDto(
        val id: String,
        val name: String,
        val start: Long,
        val end: Long,
        val category: String,
        val color: String,
        val notes: String
    )

    private fun segmentsFile(audioUri: Uri): File {
        val fileName = audioUri.lastPathSegment
            ?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            ?: "default"
        return File(context.filesDir, "$fileName.segments.json")
    }

    fun save(audioUri: Uri, segments: List<Segment>) {
        val dtos = segments.map { it.toDto() }
        segmentsFile(audioUri).writeText(json.encodeToString(dtos))
    }

    fun load(audioUri: Uri): List<Segment> {
        val file = segmentsFile(audioUri)
        if (!file.exists()) return emptyList()
        return try {
            val dtos = json.decodeFromString<List<SegmentDto>>(file.readText())
            dtos.map { it.toSegment() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Segment.toDto() = SegmentDto(id, name, start, end, category, color, notes)
    private fun SegmentDto.toSegment() = Segment(id, name, start, end, category, color, notes)
}
