package com.example.la_ruleta_de_la_suerte.ui.main.services


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.la_ruleta_de_la_suerte.R

class SonidoService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Reproducir sonido si es necesario
        mediaPlayer = MediaPlayer.create(this, R.raw.ruletasound)  // Asegúrate de tener un archivo de sonido llamado 'effect_sound.mp3'
        mediaPlayer?.start()

        // Si el servicio se detiene, liberar los recursos del reproductor
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener el sonido si el servicio es destruido
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}