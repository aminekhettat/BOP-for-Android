package org.blindsystems.bop.infra

import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.blindsystems.bop.core.Segment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SegmentRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var repo: SegmentRepository

    @BeforeEach
    fun setup() {
        context = mockk<Context>()
        every { context.filesDir } returns tempDir
        repo = SegmentRepository(context)
    }

    @Test
    fun testSaveAndLoad() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "test_audio.mp3"

        val segments = listOf(
            Segment(name = "S1", start = 0, end = 1000),
            Segment(name = "S2", start = 1000, end = 2000)
        )

        repo.save(uri, segments)

        val loaded = repo.load(uri)
        assertEquals(2, loaded.size)
        assertEquals("S1", loaded[0].name)
        assertEquals("S2", loaded[1].name)
        assertEquals(0L, loaded[0].start)
        assertEquals(1000L, loaded[0].end)
    }

    @Test
    fun testLoadNonExistent() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "missing.mp3"

        val loaded = repo.load(uri)
        assertTrue(loaded.isEmpty())
    }

    @Test
    fun testSaveEmptyList() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "empty.mp3"

        repo.save(uri, emptyList())
        val loaded = repo.load(uri)
        assertTrue(loaded.isEmpty())
    }

    @Test
    fun testLoadCorruptedFile() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "corrupt.mp3"

        // Write corrupted JSON
        File(tempDir, "corrupt.mp3.segments.json").writeText("not valid json!!!")
        val loaded = repo.load(uri)
        assertTrue(loaded.isEmpty())
    }

    @Test
    fun testSaveOverwrites() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "overwrite.mp3"

        repo.save(uri, listOf(Segment(name = "A", start = 0, end = 100)))
        repo.save(uri, listOf(Segment(name = "B", start = 0, end = 200)))

        val loaded = repo.load(uri)
        assertEquals(1, loaded.size)
        assertEquals("B", loaded[0].name)
    }

    @Test
    fun testNullLastPathSegment() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns null

        val segments = listOf(Segment(name = "NullPath", start = 0, end = 100))
        repo.save(uri, segments)

        val loaded = repo.load(uri)
        assertEquals(1, loaded.size)
        assertEquals("NullPath", loaded[0].name)
    }

    @Test
    fun testSpecialCharactersInPath() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "my file (1)/test.mp3"

        val segments = listOf(Segment(name = "Special", start = 0, end = 100))
        repo.save(uri, segments)

        val loaded = repo.load(uri)
        assertEquals(1, loaded.size)
        assertEquals("Special", loaded[0].name)
    }

    @Test
    fun testSegmentWithAllFields() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "full.mp3"

        val segments = listOf(
            Segment(
                id = "test-id",
                name = "Full",
                start = 500,
                end = 1500,
                category = "Guitar",
                color = "#FF0000",
                notes = "A tricky riff"
            )
        )
        repo.save(uri, segments)

        val loaded = repo.load(uri)
        assertEquals(1, loaded.size)
        assertEquals("test-id", loaded[0].id)
        assertEquals("Full", loaded[0].name)
        assertEquals(500L, loaded[0].start)
        assertEquals(1500L, loaded[0].end)
        assertEquals("Guitar", loaded[0].category)
        assertEquals("#FF0000", loaded[0].color)
        assertEquals("A tricky riff", loaded[0].notes)
    }
}
