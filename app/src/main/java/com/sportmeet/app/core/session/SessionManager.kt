package com.sportmeet.app.core.session

import android.content.Context
import android.content.SharedPreferences
import com.sportmeet.app.domain.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "sportmeet_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"

        private const val KEY_BASE_URL = "base_url"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var baseUrl: String
        get() = try {
            prefs.getString(KEY_BASE_URL, "http://10.0.2.2:8080/") ?: "http://10.0.2.2:8080/"
        } catch (_: Throwable) {
            "http://10.0.2.2:8080/"
        }
        set(value) {
            prefs.edit().putString(KEY_BASE_URL, value).apply()
        }

    fun saveSession(
        token: String,
        userId: String,
        name: String,
        email: String,
        phone: String,
        role: UserRole
    ) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PHONE, phone)
            putString(KEY_USER_ROLE, role.name)
            apply()
        }
    }

    val authToken: String?
        get() = try {
            prefs.getString(KEY_AUTH_TOKEN, null)
        } catch (_: Throwable) {
            null
        }

    val userId: String?
        get() = try {
            prefs.getString(KEY_USER_ID, null)
        } catch (_: Throwable) {
            null
        }

    val userName: String
        get() = try {
            prefs.getString(KEY_USER_NAME, "Guest User") ?: "Guest User"
        } catch (_: Throwable) {
            "Guest User"
        }

    val userEmail: String
        get() = try {
            prefs.getString(KEY_USER_EMAIL, "") ?: ""
        } catch (_: Throwable) {
            ""
        }

    val userPhone: String
        get() = try {
            prefs.getString(KEY_USER_PHONE, "") ?: ""
        } catch (_: Throwable) {
            ""
        }

    val userRole: UserRole
        get() {
            return try {
                val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.PLAYER.name)
                UserRole.valueOf(roleStr ?: UserRole.PLAYER.name)
            } catch (_: Throwable) {
                UserRole.PLAYER
            }
        }

    val isLoggedIn: Boolean
        get() = try {
            !authToken.isNullOrBlank()
        } catch (_: Throwable) {
            false
        }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
