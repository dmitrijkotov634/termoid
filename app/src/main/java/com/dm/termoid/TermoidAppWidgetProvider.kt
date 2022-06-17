package com.dm.termoid

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*


class TermoidAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val process = ProcessBuilder("sh")
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val writer = BufferedWriter(OutputStreamWriter(process.outputStream))

            writer.write(
                PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(
                        id.toString(), """echo Hello World
                        |exit
                        |
                    """.trimMargin()
                    )
            )

            writer.flush()

            val scope = CoroutineScope(
                Dispatchers.Default
            )

            val destroy = scope.launch {
                delay(10000L)
                process.destroy()
            }

            @Suppress("BlockingMethodInNonBlockingContext")
            scope.launch {
                process.waitFor()

                val output = try {
                    reader.readLines().joinToString(separator = "\n")
                } catch (e: IOException) {
                    e.message
                }

                destroy.cancel()

                val views = RemoteViews(context.packageName, R.layout.widget).apply {
                    setTextViewText(R.id.result, output.toString())

                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }

                    setOnClickPendingIntent(
                        R.id.result, PendingIntent.getBroadcast(
                            context, 1,
                            Intent(context, TermoidAppWidgetProvider::class.java).setAction(
                                ACTION_UPDATE
                            ), flags
                        )
                    )
                }

                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidgetComponentName = ComponentName(context!!.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE = "ACTION_UPDATE"
    }
}