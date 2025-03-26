package com.example.la_ruleta_de_la_suerte

import android.app.Application
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador

class App : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        database.jugadorDao().insertar(Jugador(1,1000))
    }
}
