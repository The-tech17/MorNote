package com.sanjana.mornote.data

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object RemoteConfigManager {

    // We remove the private val field to avoid the static context leak.
    // Instead, we access it via this property which calls the Firebase singleton safely.
    private val config get() = Firebase.remoteConfig

    fun init() {
        val configSettings = remoteConfigSettings {
            // Check for updates every hour
            minimumFetchIntervalInSeconds = 3600
        }
        config.setConfigSettingsAsync(configSettings)

        // Default values if the device is offline
        config.setDefaultsAsync(mapOf(
            "latest_news" to "Welcome to MorNote! Stay tuned for updates."
        ))
    }

    fun fetchUpdates(onComplete: (String) -> Unit) {
        config.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(config.getString("latest_news"))
            } else {
                // Fallback to existing or default value if fetch fails
                onComplete(config.getString("latest_news"))
            }
        }
    }
}