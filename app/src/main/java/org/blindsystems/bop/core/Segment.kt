package org.blindsystems.bop.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * Represents a named A-B loop segment within an audio file.
 * Mirrors the Python [Segment] dataclass from core/segment.py.
 * 
 * @property id Unique identifier for the segment (default: generated UUID).
 * @property name Human-readable name of the segment.
 * @property start Start position of the loop in milliseconds.
 * @property end End position of the loop in milliseconds.
 * @property category Optional category for organizing segments (e.g., "Intro", "Solo").
 * @property color Hex color string for visual representation (default: Light Blue).
 * @property notes Additional user notes or lyrics for this segment.
 */
@Parcelize
data class Segment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val start: Long,       // milliseconds
    val end: Long,         // milliseconds
    val category: String = "",
    val color: String = "#4FC3F7",
    val notes: String = ""
) : Parcelable {
    /**
     * Returns the length of the segment in milliseconds.
     */
    val durationMs: Long get() = end - start
}
