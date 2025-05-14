package com.example.la_ruleta_de_la_suerte.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.dao.PartidaDao
import com.example.la_ruleta_de_la_suerte.data.local.db.App
import com.example.la_ruleta_de_la_suerte.data.local.db.AppDatabase
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import com.example.la_ruleta_de_la_suerte.utils.HistorialAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HistorialActivity : AppCompatActivity() {
    private lateinit var partidaDao: PartidaDao
    private lateinit var database: AppDatabase
    private lateinit var partidas: List<Partida>
    private lateinit var backButton: ImageButton
    private lateinit var reset: Button
    private val disposables = CompositeDisposable()
    private lateinit var adapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        database = (applicationContext as App).database
        partidaDao = database.partidaDao()

        backButton = findViewById(R.id.back_button)
        reset = findViewById(R.id.reset_button)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewHistorial)

        adapter = HistorialAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        reset.setOnClickListener {
            val disp = partidaDao.eliminarTodas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("HistorialActivity", getString(R.string.logHistorialBorrado))
                    partidas = listOf()
                    recyclerView.adapter = HistorialAdapter(partidas)
                    Toast.makeText(this, getString(R.string.toastHistorialReseteado), Toast.LENGTH_SHORT).show()
                }, { error ->
                    Log.e("HistorialActivity", getString(R.string.logHistorialErrorBorrar), error)
                })
            disposables.add(disp)
        }

        val disp = partidaDao.obtenerTodas()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ partidasDB ->
                partidas = partidasDB
                recyclerView.adapter = HistorialAdapter(partidas)
                Log.d("HistorialActivity", getString(R.string.logHistorialCargado))
            }, { error ->
                Log.e("HistorialActivity", getString(R.string.logHistorialErrorCargar), error)
            })
        disposables.add(disp)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
