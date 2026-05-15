package org.blindsystems.bop.core

/**
 * Manages the ordered collection of [Segment] objects.
 * Mirrors core/segment_manager.py — add, remove, move, sort.
 */
class SegmentManager {

    private val _segments = mutableListOf<Segment>()
    val segments: List<Segment> get() = _segments.toList()

    fun add(segment: Segment) {
        _segments.add(segment)
    }

    fun remove(id: String): Boolean {
        val idx = _segments.indexOfFirst { it.id == id }
        return if (idx >= 0) { _segments.removeAt(idx); true } else false
    }

    fun moveUp(id: String): Boolean {
        val idx = _segments.indexOfFirst { it.id == id }
        if (idx <= 0) return false
        val tmp = _segments[idx - 1]
        _segments[idx - 1] = _segments[idx]
        _segments[idx] = tmp
        return true
    }

    fun moveDown(id: String): Boolean {
        val idx = _segments.indexOfFirst { it.id == id }
        if (idx < 0 || idx >= _segments.size - 1) return false
        val tmp = _segments[idx + 1]
        _segments[idx + 1] = _segments[idx]
        _segments[idx] = tmp
        return true
    }

    fun getById(id: String): Segment? = _segments.firstOrNull { it.id == id }

    fun setAll(list: List<Segment>) {
        _segments.clear()
        _segments.addAll(list)
    }

    fun clear() = _segments.clear()
}
