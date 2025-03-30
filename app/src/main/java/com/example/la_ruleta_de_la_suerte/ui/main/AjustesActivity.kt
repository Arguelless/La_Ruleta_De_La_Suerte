package com.example.la_ruleta_de_la_suerte.ui.main
import android.provider.Settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AjustesActivity : AppCompatActivity() {

    private lateinit var resetMonedas: Button
    private lateinit var database: AppDatabase
    private lateinit var jugadorDao: JugadorDao
    private lateinit var backButton: ImageButton
    private lateinit var coinsText: TextView
    private val disposables = CompositeDisposable()
    private lateinit var audioManager: AudioManager
    private lateinit var audioSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ajustes)
        database = (applicationContext as App).database
        jugadorDao = database.jugadorDao()
        resetMonedas = findViewById(R.id.resetMonedas)
        backButton = findViewById(R.id.back_button4)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioSwitch = findViewById(R.id.sonidoSwitch)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        resetMonedas.setOnClickListener {
            val disp = jugadorDao.actualizarMonedas(1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("AjustesActivity", "Monedas reseteadas")
                    Toast.makeText(this, "Monedas reseteadas", Toast.LENGTH_SHORT).show()

                }, { error ->
                    // Maneja errores si los hay

                    Log.e("AjustesActivity", "Error al resetear monedas", error)
                })
            disposables.add(disp)
        }
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> {
                audioSwitch.isChecked = false // Apagado si está en silencio o vibración
            }
            AudioManager.RINGER_MODE_NORMAL -> {
                audioSwitch.isChecked = true  // Encendido si está en modo normal
            }
        }
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent) // Redirige al usuario para que otorgue el permiso
        }

        audioSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (notificationManager.isNotificationPolicyAccessGranted) {
                if (isChecked) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL // Sonido normal

                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT // Silencio

                }
            } else {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent) // Redirige al usuario si aún no ha otorgado el permiso
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}