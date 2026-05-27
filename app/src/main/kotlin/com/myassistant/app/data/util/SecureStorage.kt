package com.myassistant.app.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val securePrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val settingsPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    // MiniMax API Key (primary)
    fun saveApiKey(key: String) = securePrefs.edit().putString(KEY_MINIMAX_API_KEY, key).apply()

    fun getApiKey(): String = securePrefs.getString(KEY_MINIMAX_API_KEY, "") ?: ""

    fun hasApiKey(): Boolean = getApiKey().isNotBlank()

    fun clearApiKey() = securePrefs.edit().remove(KEY_MINIMAX_API_KEY).apply()

    // Monitor state
    fun isMonitorEnabled(): Boolean = settingsPrefs.getBoolean(KEY_MONITOR_ENABLED, false)

    fun setMonitorEnabled(enabled: Boolean) =
        settingsPrefs.edit().putBoolean(KEY_MONITOR_ENABLED, enabled).apply()

    companion object {
        private const val KEY_MINIMAX_API_KEY = "minimax_api_key"
        private const val KEY_MONITOR_ENABLED = "monitor_enabled"
    }
}
