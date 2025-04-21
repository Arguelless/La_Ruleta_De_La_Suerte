package com.example.la_ruleta_de_la_suerte

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.ui.main.services.MusicService
import com.example.la_ruleta_de_la_suerte.ui.main.MainActivity
import com.example.la_ruleta_de_la_suerte.ui.main.ajustesActivity


class PrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)

        // Inicia la música al arrancar la app
        val intent = Intent(this, MusicService::class.java)
        startService(intent)

        findViewById<Button>(R.id.btnIniciar).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btnAjustes).setOnClickListener {
            startActivity(Intent(this, ajustesActivity::class.java))
        }

        findViewById<Button>(R.id.btnHistorial).setOnClickListener {
            // Aquí puedes abrir tu pantalla de historial cuando la tengas
        }
    }
}