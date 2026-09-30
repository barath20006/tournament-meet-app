package com.sportmeet.app

import android.app.Application
import com.sportmeet.app.core.crash.CrashHandler

class SportMeetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(this)
    }
}
