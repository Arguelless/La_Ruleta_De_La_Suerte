package com.example.la_ruleta_de_la_suerte.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ajustes)
        database = (applicationContext as App).database
        jugadorDao = database.jugadorDao()
        resetMonedas = findViewById(R.id.resetMonedas)
        backButton = findViewById(R.id.back_button4)

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

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}