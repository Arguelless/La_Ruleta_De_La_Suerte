package com.example.la_ruleta_de_la_suerte.ui.main

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.la_ruleta_de_la_suerte.R
import java.util.Random

// MainActivity.kt
class MainActivity : AppCompatActivity() {

    private lateinit var ruletaImage: ImageView
    private lateinit var botonGirar: Button
    private var anguloActual = 0f
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        ruletaImage = findViewById(R.id.ruletaImage)
        botonGirar = findViewById(R.id.botonGirar)

        botonGirar.setOnClickListener {
            girarRuleta()
        }
    }

    private fun girarRuleta() {
        // Ángulo aleatorio entre 360 y 3600 para que gire varias vueltas
        val nuevoAngulo = random.nextInt(3600) + 360

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
        // Divide el ángulo en secciones (por ejemplo, 6 premios de 60°)
        val sector = (anguloFinal / 60)  // Cambia 60 si usas más o menos secciones
        Toast.makeText(this, "¡Has caído en el sector $sector!", Toast.LENGTH_SHORT).show()
    }
}
