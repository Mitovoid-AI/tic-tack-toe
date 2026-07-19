package com.tictactoe.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RemoteConfigManager(context: Context) {

    companion object {
        private const val TAG = "RemoteConfig"
        private const val PREFS_NAME = "remote_config_cache"
        private const val KEY_CACHED_JSON = "cached_json"
        private const val CONFIG_URL = "https://raw.githubusercontent.com/Mitovoid-AI/tic-tack-toe/main/config.json"
        private val json = Json { ignoreUnknownKeys = true }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private var cachedConfig: RemoteConfig? = null

    suspend fun fetchConfig(): RemoteConfig {
        cachedConfig?.let { return it }

        val config = withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(CONFIG_URL).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val parsed = json.decodeFromString<RemoteConfig>(body)
                    prefs.edit().putString(KEY_CACHED_JSON, body).apply()
                    Log.d(TAG, "Config fetched successfully")
                    parsed
                } else {
                    loadCachedOrFallback()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch config: ${e.message}")
                loadCachedOrFallback()
            }
        }

        cachedConfig = config
        return config
    }

    private fun loadCachedOrFallback(): RemoteConfig {
        val cached = prefs.getString(KEY_CACHED_JSON, null)
        return if (cached != null) {
            try {
                json.decodeFromString<RemoteConfig>(cached)
            } catch (e: Exception) {
                Log.w(TAG, "Cached config corrupted, using defaults")
                RemoteConfig()
            }
        } else {
            Log.d(TAG, "No cached config, using defaults")
            RemoteConfig()
        }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
        cachedConfig = null
    }
}
