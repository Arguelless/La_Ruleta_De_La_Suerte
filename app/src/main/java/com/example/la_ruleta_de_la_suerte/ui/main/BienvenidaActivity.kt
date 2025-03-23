package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.R

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var botonEntrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bienvenida)

        botonEntrar = findViewById(R.id.entrar)

        botonEntrar.setOnClickListener {
            entrar()
        }
    }

    private fun entrar() {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
    }
}