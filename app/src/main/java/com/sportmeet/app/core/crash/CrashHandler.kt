package com.sportmeet.app.core.crash

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sportmeet.app.CrashReportActivity

object CrashHandler {
    private const val PREFS_NAME = "sportmeet_crash_store"
    const val KEY_LAST_CRASH_MESSAGE = "last_crash_message"
    const val KEY_LAST_CRASH_STACKTRACE = "last_crash_stacktrace"
    const val KEY_LAST_CRASH_TIME = "last_crash_time"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SportMeetCrash", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            try {
                saveCrash(context, throwable)
                showCrashScreen(context, throwable)
            } catch (t: Throwable) {
                Log.e("SportMeetCrash", "Failed to launch crash recovery screen", t)
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun saveCrash(context: Context, throwable: Throwable) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val stackTrace = Log.getStackTraceString(throwable)
            val message = throwable.localizedMessage ?: throwable.message ?: throwable.javaClass.name
            prefs.edit()
                .putString(KEY_LAST_CRASH_MESSAGE, message)
                .putString(KEY_LAST_CRASH_STACKTRACE, stackTrace)
                .putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
                .apply()
        } catch (_: Throwable) {}
    }

    fun showCrashScreen(context: Context, throwable: Throwable) {
        val stackTrace = Log.getStackTraceString(throwable)
        val message = throwable.localizedMessage ?: throwable.message ?: throwable.javaClass.name
        val intent = Intent(context, CrashReportActivity::class.java).apply {
            putExtra(CrashReportActivity.EXTRA_ERROR_MESSAGE, message)
            putExtra(CrashReportActivity.EXTRA_STACKTRACE, stackTrace)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    fun clearSavedCrash(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        } catch (_: Throwable) {}
    }
}
