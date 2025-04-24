package com.example.la_ruleta_de_la_suerte.ui.main

import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.ui.main.services.MusicService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.example.la_ruleta_de_la_suerte.data.local.db.App

class AjustesActivity : AppCompatActivity() {

    private lateinit var sonidoSwitch: SwitchCompat
    private lateinit var musicaSwitch: SwitchCompat
    private lateinit var cambiarCancionBtn: Button
    private lateinit var audioSwitch: SwitchCompat
    private lateinit var resetMonedas: Button
    private lateinit var backButton: ImageButton
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    private val disposables = CompositeDisposable()

    private var isMusicOn = true
    private var isSoundOn = true

    private val jugadorDao by lazy { (applicationContext as App).database.jugadorDao() }

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

        // Inicializar componentes
        sonidoSwitch = findViewById(R.id.sonidoSwitch2)
        musicaSwitch = findViewById(R.id.musicaSwitch)
        cambiarCancionBtn = findViewById(R.id.selecMusica)
        audioSwitch = findViewById(R.id.sonidoSwitch)
        resetMonedas = findViewById(R.id.resetMonedas)
        backButton = findViewById(R.id.back_button4)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Preferencias
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Cargar preferencia de música y aplicar estado
        isMusicOn = prefs.getBoolean("musica", true)
        musicaSwitch.isChecked = isMusicOn

        // Estado inicial del switch de audio basado en el modo del dispositivo
        audioSwitch.isChecked = audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

        audioSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (notificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = if (isChecked) {
                    AudioManager.RINGER_MODE_NORMAL
                } else {
                    AudioManager.RINGER_MODE_SILENT
                }
            } else {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        }

        sonidoSwitch.setOnCheckedChangeListener { _, isChecked ->
            isSoundOn = isChecked
            Toast.makeText(
                this,
                if (isChecked) "Efectos activados" else "Efectos desactivados",
                Toast.LENGTH_SHORT
            ).show()
            // Aquí podrías agregar lógica para activar/desactivar efectos
        }

        musicaSwitch.setOnCheckedChangeListener { _, isChecked ->
            isMusicOn = isChecked
            prefs.edit().putBoolean("musica", isChecked).apply()

            val intent = Intent(this, MusicService::class.java)
            if (isChecked) {
                startService(intent)
                Toast.makeText(this, "Música activada", Toast.LENGTH_SHORT).show()
            } else {
                stopService(intent)
                Toast.makeText(this, "Música desactivada", Toast.LENGTH_SHORT).show()
            }
        }

        cambiarCancionBtn.setOnClickListener {
            selectAudioLauncher.launch("audio/*")
        }

        resetMonedas.setOnClickListener {
            val disp = jugadorDao.actualizarMonedas(1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("AjustesActivity", "Monedas reseteadas")
                    Toast.makeText(this, "Monedas reseteadas", Toast.LENGTH_SHORT).show()
                }, { error ->
                    Log.e("AjustesActivity", "Error al resetear monedas", error)
                })
            disposables.add(disp)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Si no tiene permisos para cambiar el modo de notificaciones, redirige
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
