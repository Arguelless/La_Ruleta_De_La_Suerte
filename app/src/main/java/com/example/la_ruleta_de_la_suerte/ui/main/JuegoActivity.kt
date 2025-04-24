package com.example.la_ruleta_de_la_suerte.ui.main

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle

import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.la_ruleta_de_la_suerte.R
import java.io.IOException
import java.util.*
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.dao.PartidaDao
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Date

class JuegoActivity : AppCompatActivity() {

    private lateinit var ruletaImage: ImageView
    private lateinit var botonGirar: Button
    private lateinit var botonVictoria: Button
    private lateinit var leaveButton: Button
    private var anguloActual = 0f

    private val REQUEST_CODE_PERMISOS = 123
    private lateinit var monedas: TextView
    private lateinit var database: AppDatabase
    private lateinit var jugadorDao: JugadorDao
    private lateinit var partidaDao: PartidaDao
    private lateinit var jugador: Jugador
    private var monedasIniciales: Int = 0
    private val disposables = CompositeDisposable()
    private var monedasDB: Int = 0





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de las vistas
        setContentView(R.layout.juego)
        database = (applicationContext as App).database
        jugadorDao = database.jugadorDao()
        partidaDao = database.partidaDao()
        ruletaImage = findViewById(R.id.ruletaImage)
        botonGirar = findViewById(R.id.botonGirar)
        botonVictoria = findViewById(R.id.botonVictoria)
        leaveButton = findViewById(R.id.leaveButton)
        monedas = findViewById(R.id.coinsText2)
        inicializarJugador()

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
    val sector1 = (anguloFinal/60)
        Toast.makeText(this, "¡Has caído en el sector $sector1!", Toast.LENGTH_SHORT).show()

        val anguloAjustado = (360 - anguloFinal + 90) % 360  // 90° extra para mover el 0° a la derecha
        val sectorSize = 360f / 7
        val sector = (anguloAjustado / sectorSize).toInt()
        Log.i("zona", sector.toString())
        var mensaje = ""
        var iconoNotificacion = R.drawable.ic_victory
        when(sector) {
            0 -> {
                mensaje = "Otra vez"
            }
            1 -> {
                mensaje = "x 2"
                actualizarMonedas(monedasDB)
                iconoNotificacion = R.drawable.ic_victory
            }
            2 -> {
                mensaje = "Quiebra"
                actualizarMonedas(-monedasDB)
                iconoNotificacion = R.drawable.ic_lose
            }
            3 -> {
                mensaje = "+ 500"
                actualizarMonedas(500)
                iconoNotificacion = R.drawable.ic_victory
            }
            4 -> {
                mensaje = "- 100"
                actualizarMonedas(-100)
                iconoNotificacion = R.drawable.ic_lose
            }
            5 -> {
                mensaje = "+ 100"
                actualizarMonedas(100)
                iconoNotificacion = R.drawable.ic_victory
            }
            6 -> {
                mensaje = "- 500"
                actualizarMonedas(-500)
                iconoNotificacion = R.drawable.ic_lose
            }

        }


        Toast.makeText(this, "¡Has caído en el sector $mensaje!", Toast.LENGTH_SHORT).show()
    }

    // Función para volver a la actividad principal
    private fun volverPrincipal() {
        jugador.cantidadMonedas = monedasDB
        val disposable = jugadorDao.actualizarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("JuegoActivity", "Jugador actualizado con éxito")
            }, { error ->
                // Maneja errores si los hay
                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })
        val partida = Partida(0,System.currentTimeMillis(), monedasDB - monedasIniciales, monedasDB)
        val disposable2 = partidaDao.insertar(partida)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("JuegoActivity", "Partida insertada con éxito")
            }, { error ->
                // Maneja errores si los hay
                Log.e("JuegoActivity", "Error al insertar partida", error)
            })

        disposables.add(disposable)
        disposables.add(disposable2)
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
        val imageCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
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
    private fun inicializarJugador() {
        val disposable = jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({jugadorBD ->
                jugador = jugadorBD
                monedasDB = jugadorBD.cantidadMonedas
                monedasIniciales = jugadorBD.cantidadMonedas
                monedas.text = monedasDB.toString()
            }, { error ->
                // Maneja errores si los hay

                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })

        disposables.add(disposable)

    }

    private fun actualizarMonedas(monedasASumar: Int) {

        monedasDB += monedasASumar
        monedas.text = monedasDB.toString()
    }


    override fun onDestroy() {
        jugador.cantidadMonedas = monedasDB
        val disposable = jugadorDao.actualizarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("JuegoActivity", "Jugador actualizado con éxito")
            }, { error ->
                // Maneja errores si los hay
                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })
        super.onDestroy()


        disposables.clear() // Cancela todas las suscripciones

    }








}
