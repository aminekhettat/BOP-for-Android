package org.blindsystems.bop.infra

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bop_settings")

/**
 * Persists user settings via Jetpack DataStore.
 * Mirrors infra/settings.py.
 */
open class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_TEMPO       = floatPreferencesKey("tempo")
        val KEY_PITCH       = floatPreferencesKey("pitch")
        val KEY_THEME       = stringPreferencesKey("theme")          // DARK | LIGHT | HIGH_CONTRAST
        val KEY_LANGUAGE    = stringPreferencesKey("language")       // fr | en
        val KEY_VOLUME      = floatPreferencesKey("volume")
        val KEY_PITCH_PRESERVING = booleanPreferencesKey("pitch_preserving")
        val KEY_ANNOUNCE_INTERVAL = intPreferencesKey("announce_interval_s")
        val KEY_LAST_AUDIO  = stringPreferencesKey("last_audio_uri")
    }

    val tempo: Flow<Float>    = context.dataStore.data.map { it[KEY_TEMPO]    ?: 1.0f }
    val pitch: Flow<Float>    = context.dataStore.data.map { it[KEY_PITCH]    ?: 1.0f }
    val volume: Flow<Float>   = context.dataStore.data.map { it[KEY_VOLUME]   ?: 1.0f }
    val pitchPreserving: Flow<Boolean> = context.dataStore.data.map { it[KEY_PITCH_PRESERVING] ?: true }
    val announceInterval: Flow<Int> = context.dataStore.data.map { it[KEY_ANNOUNCE_INTERVAL] ?: 10 }
    val theme: Flow<String>   = context.dataStore.data.map { it[KEY_THEME]    ?: "DARK" }
    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "fr" }
    val lastAudioUri: Flow<String?> = context.dataStore.data.map { it[KEY_LAST_AUDIO] }

    suspend fun saveTempo(value: Float) = context.dataStore.edit { it[KEY_TEMPO] = value }
    suspend fun savePitch(value: Float) = context.dataStore.edit { it[KEY_PITCH] = value }
    suspend fun saveVolume(value: Float) = context.dataStore.edit { it[KEY_VOLUME] = value }
    suspend fun savePitchPreserving(value: Boolean) = context.dataStore.edit { it[KEY_PITCH_PRESERVING] = value }
    suspend fun saveAnnounceInterval(value: Int) = context.dataStore.edit { it[KEY_ANNOUNCE_INTERVAL] = value }
    suspend fun saveTheme(value: String) = context.dataStore.edit { it[KEY_THEME] = value }
    suspend fun saveLanguage(value: String) = context.dataStore.edit { it[KEY_LANGUAGE] = value }
    suspend fun saveLastAudioUri(value: String) = context.dataStore.edit { it[KEY_LAST_AUDIO] = value }
}
