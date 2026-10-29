package com.sportmeet.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.sportmeet.app.core.crash.CrashHandler
import com.sportmeet.app.core.session.SessionManager

class CrashReportActivity : Activity() {

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACKTRACE = "extra_stacktrace"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Unknown application error"
        val stackTrace = intent.getStringExtra(EXTRA_STACKTRACE) ?: "No stack trace available"

        // Root container with dark aesthetic matching SportMeet
        val rootScrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#0F172A"))
            isFillViewport = true
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val pad = dpToPx(20)
            setPadding(pad, dpToPx(36), pad, dpToPx(24))
        }

        // Header Emoji badge
        val badge = TextView(this).apply {
            text = "⚠️"
            textSize = 36f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dpToPx(12)
            }
        }
        container.addView(badge)

        // Title
        val titleText = TextView(this).apply {
            text = "Sports Arena Issue Detected"
            setTextColor(Color.WHITE)
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
        }
        container.addView(titleText)

        // Subtitle explanation
        val subtitleText = TextView(this).apply {
            text = "The application caught an issue during launch. Here is the diagnostic report:"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 14f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(20)
            }
        }
        container.addView(subtitleText)

        // Error message card
        val errorCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EF4444"))
            }
            background = bg
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }

        val errorLabel = TextView(this).apply {
            text = "ERROR SUMMARY"
            setTextColor(Color.parseColor("#F87171"))
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(4)
            }
        }
        errorCard.addView(errorLabel)

        val errorMsgView = TextView(this).apply {
            text = errorMessage
            setTextColor(Color.parseColor("#FCA5A5"))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }
        errorCard.addView(errorMsgView)
        container.addView(errorCard)

        // Stacktrace label
        val stackLabel = TextView(this).apply {
            text = "TRACE LOG"
            setTextColor(Color.parseColor("#64748B"))
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(6)
            }
        }
        container.addView(stackLabel)

        // Stacktrace scrollable box
        val traceBox = TextView(this).apply {
            text = stackTrace
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 11f
            typeface = Typeface.MONOSPACE
            val traceBg = GradientDrawable().apply {
                setColor(Color.parseColor("#090D16"))
                cornerRadius = dpToPx(10).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = traceBg
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            maxLines = 16
            setTextIsSelectable(true)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
        }
        container.addView(traceBox)

        // Button 1: Copy Crash Details
        val copyButton = createStyledButton("📋 Copy Error Details", Color.parseColor("#2563EB"), Color.WHITE).apply {
            setOnClickListener {
                val fullReport = "Sports Arena Crash Report:\n$errorMessage\n\nStacktrace:\n$stackTrace"
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Sports Arena Crash Log", fullReport)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@CrashReportActivity, "Error details copied to clipboard!", Toast.LENGTH_LONG).show()
            }
        }
        container.addView(copyButton)

        // Spacer
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(10))
        })

        // Button 2: Clear Session & Restart
        val resetRestartButton = createStyledButton("🔄 Reset App Data & Restart", Color.parseColor("#334155"), Color.WHITE).apply {
            setOnClickListener {
                try {
                    SessionManager.getInstance(applicationContext).clearSession()
                    CrashHandler.clearSavedCrash(applicationContext)
                } catch (_: Throwable) {}
                restartMainActivity()
            }
        }
        container.addView(resetRestartButton)

        // Spacer
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(10))
        })

        // Button 3: Restart Normally
        val restartButton = createStyledButton("🚀 Try Normal Restart", Color.parseColor("#10B981"), Color.WHITE).apply {
            setOnClickListener {
                restartMainActivity()
            }
        }
        container.addView(restartButton)

        rootScrollView.addView(container)
        setContentView(rootScrollView)
    }

    private fun restartMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun createStyledButton(text: String, bgColor: Int, textColor: Int): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            val bg = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dpToPx(12).toFloat()
            }
            background = bg
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
