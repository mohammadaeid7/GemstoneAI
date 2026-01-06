package com.example.gemstoneai.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gemstone_ai_prefs")

class UserPrefsRepository(private val context: Context) {

    private val KEY_CURRENCY = stringPreferencesKey("currency")
    private val KEY_RATE_TO_USD = doublePreferencesKey("rate_to_usd")
    private val KEY_HISTORY = stringPreferencesKey("history_csv")

    val currencyFlow: Flow<String> = context.dataStore.data.map { it[KEY_CURRENCY] ?: "USD" }
    val rateToUsdFlow: Flow<Double> = context.dataStore.data.map { it[KEY_RATE_TO_USD] ?: 1.0 }

    val historyFlow: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_HISTORY] ?: ""
        if (raw.isBlank()) return@map emptyList()
        raw.split("\n")
            .mapNotNull { HistoryEntry.fromCsv(it) }
            .sortedByDescending { it.timestampMillis }
    }

    suspend fun saveCurrency(currency: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = currency }
    }

    suspend fun saveRateToUsd(rateToUsd: Double) {
        context.dataStore.edit { it[KEY_RATE_TO_USD] = rateToUsd }
    }

    suspend fun addHistory(entry: HistoryEntry) {
        context.dataStore.edit { prefs ->
            val existing = prefs[KEY_HISTORY] ?: ""
            val newValue = if (existing.isBlank()) entry.toCsv() else (entry.toCsv() + "\n" + existing)
            prefs[KEY_HISTORY] = newValue
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[KEY_HISTORY] = "" }
    }
}
