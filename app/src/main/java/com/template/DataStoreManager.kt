package com.template

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences_name")
        val IS_IN_SERVER_KEY = intPreferencesKey("is_in_server_key")
        val SERVER_URL_KEY = stringPreferencesKey("server_url_key")
    }

    suspend fun updateIsInServer(isInServer: Int) {
        context.dataStore.edit { preferences ->
            preferences[IS_IN_SERVER_KEY] = isInServer
        }
    }
    suspend fun updateServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }
    val getIsInServer = context.dataStore.data.map {
        it[IS_IN_SERVER_KEY] ?: 0
    }
    val getServerUrl = context.dataStore.data.map {
        it[SERVER_URL_KEY] ?: ""
    }
}