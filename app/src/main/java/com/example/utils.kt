package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.View
import java.util.Calendar
import java.util.TimeZone

fun captureScreen(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun saveVictoryScreenshot(bitmap: Bitmap, context: Context) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "victoria_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RuletaVictoria")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        context.contentResolver.openOutputStream(it).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output!!)
        }
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)
    }
}

fun addVictoryToCalendar(context: Context) {
    val calendar = Calendar.getInstance()
    val startMillis = calendar.timeInMillis
    val endMillis = startMillis + 60 * 60 * 1000  // 1 hora

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.TITLE, "¡Victoria en la Ruleta de la Suerte!")
        put(CalendarContract.Events.DESCRIPTION, "¡Hugo ha ganado una partida!")
        put(CalendarContract.Events.CALENDAR_ID, 1)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
}
