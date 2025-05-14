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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.ui.main.services.MusicService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class AjustesActivity : AppCompatActivity() {

    private lateinit var sonidoSwitch: SwitchCompat
    private lateinit var musicaSwitch: SwitchCompat
    private lateinit var cambiarCancionBtn: Button
    private lateinit var audioSwitch: SwitchCompat
    private lateinit var resetMonedas: Button
    private lateinit var backButton: ImageButton
    private lateinit var idiomaBtn: Button
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
            Toast.makeText(this, getString(R.string.toast_reproduciendo_cancion), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Restaurar idioma guardado
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.getString("idioma", null)?.let {
            val locale = Locale(it)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.ajustes)

        // Inicializar componentes
        sonidoSwitch = findViewById(R.id.sonidoSwitch2)
        musicaSwitch = findViewById(R.id.musicaSwitch)
        cambiarCancionBtn = findViewById(R.id.selecMusica)
        audioSwitch = findViewById(R.id.sonidoSwitch)
        resetMonedas = findViewById(R.id.resetMonedas)
        backButton = findViewById(R.id.back_button4)
        idiomaBtn = findViewById(R.id.selecIdioma)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Preferencias
        isMusicOn = prefs.getBoolean("musica", true)
        musicaSwitch.isChecked = isMusicOn

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
            val msgRes = if (isChecked) R.string.toast_efectos_activados else R.string.toast_efectos_desactivados
            Toast.makeText(this, getString(msgRes), Toast.LENGTH_SHORT).show()
        }

        musicaSwitch.setOnCheckedChangeListener { _, isChecked ->
            isMusicOn = isChecked
            prefs.edit().putBoolean("musica", isChecked).apply()

            val intent = Intent(this, MusicService::class.java)
            if (isChecked) {
                startService(intent)
                Toast.makeText(this, getString(R.string.toast_musica_activada), Toast.LENGTH_SHORT).show()
            } else {
                stopService(intent)
                Toast.makeText(this, getString(R.string.toast_musica_desactivada), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, getString(R.string.toast_monedas_reseteadas), Toast.LENGTH_SHORT).show()
                }, { error ->
                    Log.e("AjustesActivity", "Error al resetear monedas", error)
                    Toast.makeText(this, getString(R.string.toast_error_reset), Toast.LENGTH_SHORT).show()
                })
            disposables.add(disp)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        idiomaBtn.setOnClickListener {
            mostrarSelectorDeIdioma()
        }

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    private fun mostrarSelectorDeIdioma() {
        val idiomas = arrayOf(
            getString(R.string.selector_idioma_op1),
            getString(R.string.selector_idioma_op2),
            getString(R.string.selector_idioma_op3),
            getString(R.string.selector_idioma_op4)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.selector_idioma_title))
            .setItems(idiomas) { _, which ->
                when (which) {
                    0 -> {
                        getSharedPreferences("settings", MODE_PRIVATE).edit()
                            .remove("idioma")
                            .apply()
                        Toast.makeText(this, getString(R.string.toast_idioma_sistema), Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                    1 -> cambiarIdioma("es")
                    2 -> cambiarIdioma("en")
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun cambiarIdioma(codigoIdioma: String) {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("idioma", codigoIdioma)
            .apply()

        Toast.makeText(this, getString(R.string.toast_idioma_cambiado, locale.displayLanguage), Toast.LENGTH_SHORT).show()
        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
