package com.example.la_ruleta_de_la_suerte.ui.main

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.R

class HelpActivity : AppCompatActivity() {
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Inicializar WebView y cargar el archivo HTML
        val webView: WebView = findViewById(R.id.webViewHelp)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/help.html")

        // Configurar el botón "Volver"
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            // Al presionar el botón "Volver", finalizamos la actividad y volvemos al menú principal
            finish()
        }
    }

    // Opcional: Manejar el comportamiento del botón de "Atrás" físico
    override fun onBackPressed() {
        super.onBackPressed()  // Esto regresa a la actividad anterior
    }
}

