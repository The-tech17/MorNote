package com.sanjana.mornote.security

import android.content.Context
import android.os.Build
import android.util.Log
import com.scottyab.rootbeer.RootBeer
import java.io.File

object SecurityGuard {
    private const val TAG = "SecurityGuard"

    enum class SecurityPolicy {
        ALLOW,
        BLOCK_ENTRY,
        LOCK_NOTES,
        EMERGENCY_WIPE
    }

    private fun logW(tag: String, msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            Log.e(tag, msg, tr)
        } catch (_: Throwable) {
            println("[$tag] $msg ${tr?.message ?: ""}")
        }
    }

    /**
     * Configurable security policy enforced when device compromise or root access is detected.
     */
    var activePolicy: SecurityPolicy = SecurityPolicy.BLOCK_ENTRY

    /**
     * Checks if the device is rooted or compromised via RootBeer checks, build tags,
     * su binary paths, and Magisk indicators.
     *
     * @param context Application or Activity context
     * @return true if device root or tampering is detected, false otherwise
     */
    fun isDeviceCompromised(context: Context): Boolean {
        var isCompromised = false

        // 1. RootBeer Library Inspection
        try {
            val rootBeer = RootBeer(context)
            if (rootBeer.isRooted) {
                logW(TAG, "[SECURITY ALERT] RootBeer check returned positive for root access.")
                isCompromised = true
            }
        } catch (e: Throwable) {
            logE(TAG, "RootBeer verification error", e)
        }

        // 2. Build Tags Check (test-keys / custom ROMs)
        if (!isCompromised) {
            try {
                val buildTags = Build.TAGS
                if (buildTags != null && buildTags.contains("test-keys")) {
                    logW(TAG, "[SECURITY ALERT] Build tags contain 'test-keys'.")
                    isCompromised = true
                }
            } catch (e: Throwable) {
                logE(TAG, "Build tags check error", e)
            }
        }

        // 3. Known Su Binaries Location Check
        if (!isCompromised) {
            val suPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in suPaths) {
                try {
                    if (File(path).exists()) {
                        logW(TAG, "[SECURITY ALERT] Found su binary at path: $path")
                        isCompromised = true
                        break
                    }
                } catch (_: Throwable) {}
            }
        }

        // 4. Magisk Paths & Framework Check
        if (!isCompromised) {
            val magiskPaths = arrayOf(
                "/sbin/.magisk",
                "/data/adb/magisk",
                "/data/magisk",
                "/cache/magisk.log"
            )
            for (path in magiskPaths) {
                try {
                    if (File(path).exists()) {
                        logW(TAG, "[SECURITY ALERT] Found Magisk directory/file at path: $path")
                        isCompromised = true
                        break
                    }
                } catch (_: Throwable) {}
            }
        }

        if (isCompromised) {
            logW(TAG, "[SECURITY WARNING] Device detected as rooted/compromised! Active policy: $activePolicy")
        }

        return isCompromised
    }
}
