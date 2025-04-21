package com.example.la_ruleta_de_la_suerte.ui.main.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import androidx.core.net.toUri
import com.example.la_ruleta_de_la_suerte.R

class MusicService : Service() {

    companion object {
        var currentSongUri: Uri? = null
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songUri = intent?.getStringExtra("songUri")?.toUri()

        if (songUri != null && songUri != currentSongUri) {
            currentSongUri = songUri
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, songUri)
                isLooping = true
                prepare()
                start()
            }
        } else if (mediaPlayer == null) {
            val defaultSongUri = Uri.parse("android.resource://${packageName}/${R.raw.smch_droid}")
            mediaPlayer = MediaPlayer.create(this, currentSongUri ?: defaultSongUri)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}