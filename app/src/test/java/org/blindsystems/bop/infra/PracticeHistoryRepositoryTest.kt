package org.blindsystems.bop.infra

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PracticeHistoryRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var repo: PracticeHistoryRepository

    @BeforeEach
    fun setup() {
        context = mockk<Context>()
        every { context.filesDir } returns tempDir
        repo = PracticeHistoryRepository(context)
    }

    @Test
    fun testLogAndLoad() {
        repo.logSession("test.mp3", 60000, 10, 1.2f)

        val history = repo.loadAll()
        assertEquals(1, history.size)
        assertEquals("test.mp3", history[0].audioFile)
        assertEquals(10, history[0].loopCount)
        assertEquals(120, history[0].tempoPercent)
        assertEquals(60000L, history[0].durationMs)
    }

    @Test
    fun testLoadEmpty() {
        val history = repo.loadAll()
        assertTrue(history.isEmpty())
    }

    @Test
    fun testMultipleSessions() {
        repo.logSession("a.mp3", 1000, 5, 1.0f)
        repo.logSession("b.mp3", 2000, 10, 1.5f)
        repo.logSession("c.mp3", 3000, 15, 0.8f)

        val history = repo.loadAll()
        assertEquals(3, history.size)
        assertEquals("a.mp3", history[0].audioFile)
        assertEquals("b.mp3", history[1].audioFile)
        assertEquals("c.mp3", history[2].audioFile)
    }

    @Test
    fun testExportCsv() {
        repo.logSession("test.mp3", 5000, 2, 1.0f)

        val path = repo.exportCsv()
        val file = File(path)
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue(content.contains("Date,Audio File,Duration (s),Loop Count,Tempo (%)"))
        assertTrue(content.contains("test.mp3"))
        assertTrue(content.contains("5")) // 5000/1000 = 5
        assertTrue(content.contains("2"))
        assertTrue(content.contains("100"))
    }

    @Test
    fun testExportCsvEmpty() {
        val path = repo.exportCsv()
        val file = File(path)
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue(content.contains("Date,Audio File"))
        // Should only have header
        assertEquals(2, content.lines().size) // header + trailing newline
    }

    @Test
    fun testLoadCorruptedFile() {
        File(tempDir, "practice_history.json").writeText("not json at all!!!")
        val history = repo.loadAll()
        assertTrue(history.isEmpty())
    }

    @Test
    fun testSessionRecordData() {
        val record = PracticeHistoryRepository.SessionRecord(
            date = "2026-01-01 12:00:00",
            audioFile = "song.mp3",
            durationMs = 120000,
            loopCount = 20,
            tempoPercent = 150
        )
        assertEquals("2026-01-01 12:00:00", record.date)
        assertEquals("song.mp3", record.audioFile)
        assertEquals(120000L, record.durationMs)
        assertEquals(20, record.loopCount)
        assertEquals(150, record.tempoPercent)
    }

    @Test
    fun testDateFormatInLog() {
        repo.logSession("dated.mp3", 1000, 1, 1.0f)
        val history = repo.loadAll()
        assertTrue(history[0].date.isNotEmpty())
        // Date should match yyyy-MM-dd HH:mm:ss pattern
        assertTrue(history[0].date.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }
}
