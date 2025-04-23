package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.ui.main.services.MusicService

class ajustesActivity : AppCompatActivity() {

    private lateinit var effectsSeekBar: SeekBar
    private lateinit var musicSeekBar: SeekBar
    private lateinit var muteEffectsBtn: ImageButton
    private lateinit var muteMusicBtn: ImageButton
    private lateinit var selectSongBtn: Button

    private var isMusicMuted = false
    private var isEffectsMuted = false

    private val selectAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, MusicService::class.java).apply {
                putExtra("songUri", it.toString())
            }
            startService(intent)
            Toast.makeText(this, "Reproduciendo canción seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ajustes)

        effectsSeekBar = findViewById(R.id.seekBarEffects)
        musicSeekBar = findViewById(R.id.seekBarMusic)
        muteEffectsBtn = findViewById(R.id.btnMuteEffects)
        muteMusicBtn = findViewById(R.id.btnMuteMusic)
        selectSongBtn = findViewById(R.id.btnSelectSong)

        // Volúmenes iniciales
        effectsSeekBar.progress = 80
        musicSeekBar.progress = 70

        effectsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                // Lógica de efectos (si usas SoundPool)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        musicSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aquí puedes añadir comunicación con el servicio para cambiar volumen si lo implementas
                // Pero por ahora lo dejamos fijo o lo manejas dentro del servicio
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        muteEffectsBtn.setOnClickListener {
            isEffectsMuted = !isEffectsMuted
            Toast.makeText(this, if (isEffectsMuted) "Efectos silenciados" else "Efectos activados", Toast.LENGTH_SHORT).show()
        }

        muteMusicBtn.setOnClickListener {
            isMusicMuted = !isMusicMuted
            // Solo mostramos mensaje por ahora. Puedes enviar un Intent para pausar/reanudar en el servicio
            Toast.makeText(this, if (isMusicMuted) "Música silenciada" else "Música activada", Toast.LENGTH_SHORT).show()
        }

        selectSongBtn.setOnClickListener {
            selectAudioLauncher.launch("audio/*")
        }
    }
}
