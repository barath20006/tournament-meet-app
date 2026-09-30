package com.sportmeet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sportmeet.app.core.navigation.SportMeetNavGraph
import com.sportmeet.app.core.theme.SportMeetTheme

import android.util.Log
import com.sportmeet.app.core.crash.CrashHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            Log.w("SportMeet", "EdgeToEdge initialization caught non-fatal issue", t)
        }

        try {
            setContent {
                SportMeetTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SportMeetNavGraph()
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e("SportMeet", "Fatal error during setContent", t)
            CrashHandler.showCrashScreen(this, t)
        }
    }
}
