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
import android.view.LayoutInflater
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
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.dao.PartidaDao
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import com.example.la_ruleta_de_la_suerte.data.local.model.PlayerScore
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone

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
        solicitarPermisos()

        botonGirar.setOnClickListener { girarRuleta() }
        botonVictoria.setOnClickListener { guardarVictoria() }
        leaveButton.setOnClickListener { volverPrincipal() }
    }

    private fun girarRuleta() {
        val nuevoAngulo = (360..3960).random()
        val rotate = RotateAnimation(
            anguloActual,
            anguloActual + nuevoAngulo.toFloat(),
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 3000
            fillAfter = true
            interpolator = DecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
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
        }
        ruletaImage.startAnimation(rotate)
    }

    private fun mostrarResultado(anguloFinal: Int) {
        val anguloAjustado = (360 - anguloFinal + 90) % 360
        val sectorSize = 360f / 7
        val sector = (anguloAjustado / sectorSize).toInt()

        val mensaje: String
        var iconoNotificacion = R.drawable.ic_victory

        when (sector) {
            0 -> mensaje = getString(R.string.otraVez)
            1 -> {
                mensaje = getString(R.string.sectorX2)
                actualizarMonedas(monedasDB)
            }
            2 -> {
                mensaje = getString(R.string.Quiebra)
                actualizarMonedas(-monedasDB)
                iconoNotificacion = R.drawable.ic_lose
            }
            3 -> {
                mensaje = getString(R.string.sector_mas_500)
                actualizarMonedas(500)
            }
            4 -> {
                mensaje = getString(R.string.sector_menos_100)
                actualizarMonedas(-100)
                iconoNotificacion = R.drawable.ic_lose
            }
            5 -> {
                mensaje = getString(R.string.sector_mas_100)
                actualizarMonedas(100)
            }
            6 -> {
                mensaje = getString(R.string.sector_menos_500)
                actualizarMonedas(-500)
                iconoNotificacion = R.drawable.ic_lose
            }
            else -> mensaje = ""
        }

        val layout = LayoutInflater.from(this).inflate(R.layout.toast_con_icono, null)
        layout.findViewById<TextView>(R.id.toast_text).text = mensaje
        layout.findViewById<ImageView>(R.id.toast_icon).setImageResource(iconoNotificacion)

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

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
        val user = FirebaseAuth.getInstance().currentUser?.displayName


        enviarPuntuacion(user.toString(),partida.diferenciaMonedas)
        disposables.add(disposable)
        disposables.add(disposable2)
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
        val partida = Partida(0, System.currentTimeMillis(), monedasDB - monedasIniciales, monedasDB)

        val updateJugador = jugadorDao.actualizarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { error -> Log.e("JuegoActivity", "Error al actualizar jugador", error) })

        val insertarPartida = partidaDao.insertar(partida)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { error -> Log.e("JuegoActivity", "Error al insertar partida", error) })

        disposables.addAll(updateJugador, insertarPartida)
        startActivity(Intent(this, PrincipalActivity::class.java))
    }

    private fun guardarVictoria() {
        val screenshot = captureScreen(window.decorView.findViewById(android.R.id.content))
        saveVictoryScreenshot(screenshot)
        addVictoryToCalendar()
        Toast.makeText(this, getString(R.string.toastVictoria), Toast.LENGTH_SHORT).show()
    }

    private fun captureScreen(view: View): Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
        Canvas(this).apply { view.draw(this) }
    }

    private fun solicitarPermisos() {
        val permisos = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_CALENDAR)
            add(Manifest.permission.READ_CALENDAR)
        }

        val noConcedidos = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (noConcedidos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, noConcedidos.toTypedArray(), REQUEST_CODE_PERMISOS)
        }
    }

    private fun saveVictoryScreenshot(screenshot: Bitmap) {
        val resolver = contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "victoria_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = resolver.insert(imageCollection, values)
        if (imageUri != null) {
            try {
                resolver.openOutputStream(imageUri)?.use {
                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                Toast.makeText(this, getString(R.string.toast_guardado_galeria), Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, getString(R.string.toast_guardado_error), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_uri_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addVictoryToCalendar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            val calendar = Calendar.getInstance()

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                put(CalendarContract.Events.DTEND, calendar.timeInMillis + DateUtils.HOUR_IN_MILLIS)
                put(CalendarContract.Events.TITLE, getString(R.string.evento_titulo))
                put(CalendarContract.Events.DESCRIPTION, getString(R.string.evento_descripcion))
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val mensaje = if (uri != null) R.string.toast_evento_calendario_exito else R.string.toast_evento_calendario_error
            Toast.makeText(this, getString(mensaje), Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CALENDAR),
                REQUEST_CODE_PERMISOS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISOS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addVictoryToCalendar()
        } else {
            Toast.makeText(this, getString(R.string.toast_permiso_calendario_denegado), Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarJugador() {
        val disposable = jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorBD ->
                jugador = jugadorBD
                monedasDB = jugadorBD.cantidadMonedas
                monedasIniciales = jugadorBD.cantidadMonedas
                monedas.text = monedasDB.toString()
            }, { error ->
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
            .subscribe({}, { error -> Log.e("JuegoActivity", "Error al actualizar jugador", error) })
        disposables.add(disposable)
        disposables.clear()
        super.onDestroy()
    }
}
