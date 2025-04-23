package com.example.la_ruleta_de_la_suerte.ui.main

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.la_ruleta_de_la_suerte.R
import java.io.IOException
import java.util.*

class JuegoActivity : AppCompatActivity() {

    private lateinit var ruletaImage: ImageView
    private lateinit var botonGirar: Button
    private lateinit var botonVictoria: Button
    private lateinit var leaveButton: Button
    private var anguloActual = 0f

    private val REQUEST_CODE_PERMISOS = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        // Inicialización de las vistas
        ruletaImage = findViewById(R.id.ruletaImage)
        botonGirar = findViewById(R.id.botonGirar)
        botonVictoria = findViewById(R.id.botonVictoria)
        leaveButton = findViewById(R.id.leaveButton)

        // Acción al presionar el botón de girar
        botonGirar.setOnClickListener {
            girarRuleta()
        }

        // Acción al presionar el botón de victoria
        botonVictoria.setOnClickListener {
            guardarVictoria()
        }

        // Acción al presionar el botón de salir
        leaveButton.setOnClickListener {
            volverPrincipal()
        }

        // Solicitar permisos necesarios
        solicitarPermisos()
    }

    // Función para girar la ruleta
    private fun girarRuleta() {
        val nuevoAngulo = (360..3960).random()

        val rotate = RotateAnimation(
            anguloActual,
            anguloActual + nuevoAngulo.toFloat(),
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotate.duration = 3000
        rotate.fillAfter = true
        rotate.interpolator = DecelerateInterpolator()

        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                botonGirar.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animation?) {
                botonGirar.isEnabled = true
                val resultado = ((anguloActual + nuevoAngulo) % 360).toInt()
                mostrarResultado(resultado)
                anguloActual = (anguloActual + nuevoAngulo) % 360
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        ruletaImage.startAnimation(rotate)
    }

    // Función para mostrar el resultado de la ruleta
    private fun mostrarResultado(anguloFinal: Int) {
        val sector = (anguloFinal / 60)
        Toast.makeText(this, "¡Has caído en el sector $sector!", Toast.LENGTH_SHORT).show()
    }

    // Función para volver a la actividad principal
    private fun volverPrincipal() {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
    }

    // Función para guardar la victoria
    private fun guardarVictoria() {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        val screenshot = captureScreen(rootView)

        // Guardar la captura de pantalla y añadir al calendario
        saveVictoryScreenshot(screenshot, this)
        addVictoryToCalendar(this)

        Toast.makeText(this, "Victoria guardada y añadida al calendario", Toast.LENGTH_SHORT).show()
    }

    // Función para capturar la vista de la actividad
    private fun captureScreen(view: View): Bitmap {
        // Crear un bitmap del tamaño de la vista
        val screenshot = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        // Crear un lienzo donde dibujar la vista
        val canvas = Canvas(screenshot)
        // Dibujar la vista sobre el lienzo
        view.draw(canvas)
        return screenshot
    }

    // Función para solicitar los permisos necesarios
    private fun solicitarPermisos() {
        val permisos = mutableListOf<String>()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        permisos.add(Manifest.permission.WRITE_CALENDAR)
        permisos.add(Manifest.permission.READ_CALENDAR)

        val permisosNoConcedidos = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisosNoConcedidos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisosNoConcedidos.toTypedArray(), REQUEST_CODE_PERMISOS)
        }
    }

    // Función para guardar la captura de pantalla
    private fun saveVictoryScreenshot(screenshot: Bitmap, context: JuegoActivity) {
        val contentResolver = context.contentResolver
        val imageCollection: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "victoria_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(imageCollection, values)

        if (imageUri != null) {
            try {
                contentResolver.openOutputStream(imageUri).use { outputStream ->
                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                Toast.makeText(context, "Captura guardada en la galería", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al guardar la captura", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error al obtener URI de la galería", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para agregar la victoria al calendario
    private fun addVictoryToCalendar(context: JuegoActivity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            val calendar = Calendar.getInstance()

            val eventValues = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                put(CalendarContract.Events.DTEND, calendar.timeInMillis + DateUtils.HOUR_IN_MILLIS)
                put(CalendarContract.Events.TITLE, "Victoria en la Ruleta de la Suerte")
                put(CalendarContract.Events.DESCRIPTION, "¡Felicidades! Has ganado en la ruleta de la suerte.")
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)

            if (uri != null) {
                Toast.makeText(context, "Victoria añadida al calendario", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al añadir victoria al calendario", Toast.LENGTH_SHORT).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_CALENDAR),
                REQUEST_CODE_PERMISOS
            )
        }
    }

    // Manejo de la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISOS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addVictoryToCalendar(this)
            } else {
                Toast.makeText(this, "Permiso denegado, no se puede añadir victoria al calendario", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
