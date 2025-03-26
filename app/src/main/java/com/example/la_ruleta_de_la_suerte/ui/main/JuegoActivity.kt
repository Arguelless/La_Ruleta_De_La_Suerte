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
import com.example.la_ruleta_de_la_suerte.App
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.DisposableContainer
import io.reactivex.rxjava3.schedulers.Schedulers

class JuegoActivity : AppCompatActivity() {

    private lateinit var ruletaImage: ImageView
    private lateinit var botonGirar: Button
    private var anguloActual = 0f
    private lateinit var leaveButton: Button
    private lateinit var monedas: TextView
    private val partidaDao = App.database.partidaDao()
    private val jugadorDao = App.database.jugadorDao()
    private val disposables = CompositeDisposable()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        ruletaImage = findViewById(R.id.ruletaImage)
        botonGirar = findViewById(R.id.botonGirar)
        leaveButton = findViewById(R.id.leaveButton)
        monedas = findViewById(R.id.coinsText2)

        inicializarJugador()
        cargarMonedas()
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
        Log.i("a",anguloFinal.toString())
        val sectorSize = 360f / 7
        val sector = (anguloAjustado / sectorSize).toInt()
        Log.i("zona", sector.toString())
        var mensaje = ""
        when(sector) {
            0 -> {
                mensaje = "Otra vez"
                actualizarMonedas(100)
            }
            1 -> {
                mensaje = "x 2"
                actualizarMonedas(100)
            }
            2 -> {
                mensaje = "Quiebra"
                actualizarMonedas(100)
            }
            3 -> {
                mensaje = "+ 500"
                actualizarMonedas(100)
            }
            4 -> {
                mensaje = "- 100"
                actualizarMonedas(100)
            }
            5 -> {
                mensaje = "+ 100"
                actualizarMonedas(100)
            }
            6 -> {
                mensaje = "- 500"
                actualizarMonedas(100)
            }

        }


        Toast.makeText(this, "¡Has caído en el sector $mensaje!", Toast.LENGTH_SHORT).show()
    }

    private fun volverPrincipal() {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
    }

    private fun inicializarJugador() {
        jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io()) // Ejecutamos la obtención del jugador en un hilo en segundo plano
            .observeOn(AndroidSchedulers.mainThread()) // Actualizamos la UI en el hilo principal
            .subscribe({ jugador ->
                // Si ya hay un jugador, no hacemos nada
                if (jugador == null) {
                    // Si no hay jugador, creamos uno con 100 monedas por defecto
                    jugadorDao.insertar(Jugador(id = 1, cantidadMonedas = 100))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("MainActivity", "Jugador creado con 100 monedas")
                        }, { error ->
                            Log.e("MainActivity", "Error al insertar jugador", error)
                        }).let { disposables.add(it) } // Aseguramos que el Disposable se añade al CompositeDisposable
                }
            }, { error ->
                Log.e("MainActivity", "Error al obtener jugador", error)
            }).let { disposables.add(it) } // Aseguramos que el Disposable se añade al CompositeDisposable
    }

    private fun cargarMonedas() {
        jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugador ->
                if (jugador != null) {
                    monedas.text = "Monedas: ${jugador.cantidadMonedas}"
                }
            }, { error ->
                Log.e("MainActivity", "Error al obtener monedas", error)
            }).let { disposables.add(it) } // Aseguramos que el Disposable se añade al CompositeDisposable
    }

    fun actualizarMonedas(diferencia: Int) {
        jugadorDao.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { jugador ->
                val nuevasMonedas = jugador.cantidadMonedas + diferencia
                jugadorDao.actualizarMonedas(nuevasMonedas) // Actualiza las monedas en la base de datos
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                cargarMonedas() // Actualizamos el TextView con el nuevo valor de monedas
            }, { error ->
                Log.e("MainActivity", "Error actualizando monedas", error)
            }).let { disposables.add(it) } // Aseguramos que el Disposable se añade al CompositeDisposable
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear() // Limpiamos todos los Disposables al destruir la actividad
    }
}
