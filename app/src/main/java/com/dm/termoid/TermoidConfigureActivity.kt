package com.dm.termoid

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.dm.termoid.databinding.ActivityConfigureBinding


class TermoidConfigureActivity : AppCompatActivity() {

    private val binding: ActivityConfigureBinding by lazy {
        ActivityConfigureBinding.inflate(layoutInflater)
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        binding.command.editText?.setText(preferences.getString(appWidgetId.toString(), ""))

        binding.apply.setOnClickListener {
            preferences.edit {
                putString(appWidgetId.toString(), binding.command.editText?.text.toString())

                val resultValue =
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                sendBroadcast(
                    Intent(
                        this@TermoidConfigureActivity,
                        TermoidAppWidgetProvider::class.java
                    ).setAction(TermoidAppWidgetProvider.ACTION_UPDATE)
                )

                setResult(RESULT_OK, resultValue)
                finish()
            }
        }

        setResult(RESULT_CANCELED)
    }
}