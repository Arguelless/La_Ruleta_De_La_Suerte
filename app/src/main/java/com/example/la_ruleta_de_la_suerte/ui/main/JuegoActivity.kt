package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.R
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
    private var anguloActual = 0f
    private lateinit var leaveButton: Button
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
        leaveButton = findViewById(R.id.leaveButton)
        monedas = findViewById(R.id.coinsText2)
        inicializarJugador()

        botonGirar.setOnClickListener {
            girarRuleta()
        }
        leaveButton.setOnClickListener {
            volverPrincipal()
        }
    }

    private fun girarRuleta() {
        // Ángulo aleatorio entre 360 y 3600 para que gire varias vueltas
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

    private fun mostrarResultado(anguloFinal: Int) {

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
