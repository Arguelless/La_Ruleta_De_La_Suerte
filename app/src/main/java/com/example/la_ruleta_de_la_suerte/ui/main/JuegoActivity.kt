package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.R

class JuegoActivity : AppCompatActivity() {

    private lateinit var ruletaImage: ImageView
    private lateinit var botonGirar: Button
    private var anguloActual = 0f
    private lateinit var leaveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        ruletaImage = findViewById(R.id.ruletaImage)
        botonGirar = findViewById(R.id.botonGirar)
        leaveButton = findViewById(R.id.leaveButton)

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
        // Divide el ángulo en secciones (por ejemplo, 6 premios de 60°)
        val sector = (anguloFinal / 60)  // Cambia 60 si usas más o menos secciones
        Toast.makeText(this, "¡Has caído en el sector $sector!", Toast.LENGTH_SHORT).show()
    }

    private fun volverPrincipal() {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
    }
}
