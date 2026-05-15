package org.blindsystems.bop.infra

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logs practice sessions and exports to CSV.
 * Mirrors infra/practice_history.py.
 */
open class PracticeHistoryRepository(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val historyFile get() = File(context.filesDir, "practice_history.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    @Serializable
    data class SessionRecord(
        val date: String,
        val audioFile: String,
        val durationMs: Long,
        val loopCount: Int,
        val tempoPercent: Int
    )

    fun logSession(audioFileName: String, durationMs: Long, loopCount: Int, tempo: Float) {
        val record = SessionRecord(
            date = dateFormat.format(Date()),
            audioFile = audioFileName,
            durationMs = durationMs,
            loopCount = loopCount,
            tempoPercent = (tempo * 100).toInt()
        )
        val all = loadAll().toMutableList()
        all.add(record)
        historyFile.writeText(json.encodeToString(all))
    }

    fun loadAll(): List<SessionRecord> {
        if (!historyFile.exists()) return emptyList()
        return try {
            json.decodeFromString(historyFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Audio File,Duration (s),Loop Count,Tempo (%)")
        loadAll().forEach { r ->
            sb.appendLine("${r.date},${r.audioFile},${r.durationMs / 1000},${r.loopCount},${r.tempoPercent}")
        }
        val csvFile = File(context.filesDir, "practice_history.csv")
        csvFile.writeText(sb.toString())
        return csvFile.absolutePath
    }
}
