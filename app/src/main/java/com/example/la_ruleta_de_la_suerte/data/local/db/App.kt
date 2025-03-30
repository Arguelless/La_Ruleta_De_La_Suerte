package com.example.la_ruleta_de_la_suerte.data.local.db

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class App : Application() {

    lateinit var database: AppDatabase
    private val disposables = CompositeDisposable()


    override fun onCreate() {
        super.onCreate()
        // Inicializa correctamente la base de datos
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "ruleta_db"
        ).build()

        Log.d("App", "onCreate() is called and database is initialized")
        val disposable = database.jugadorDao().obtenerJugador()

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({jugadorBD ->
                Log.i("App", "jugador encontrado $jugadorBD")
            }, { error ->
                // Maneja errores si los hay
                val disposable2 = database.jugadorDao().insertar(Jugador(1, 1000))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.i("JuegoActivity", "Insertado correctamente")
                    },{ error ->
                        Log.e("JuegoActivity", "Error al insertar jugador", error)
                    })

                disposables.add(disposable2)
                Log.e("JuegoActivity", "Error al obtener jugador", error)
            })
        disposables.add(disposable)

    }

    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
        Log.d("App", "onTerminate() is called and disposables are cleared")
    }

}