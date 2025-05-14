package com.example.la_ruleta_de_la_suerte.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.db.RetrofitInstance.firebaseApi
import com.example.la_ruleta_de_la_suerte.data.local.model.PlayerScore
import com.example.la_ruleta_de_la_suerte.utils.HistorialAdapter
import com.example.la_ruleta_de_la_suerte.utils.TopGlobalAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopGlobalActivity : AppCompatActivity()  {

    private lateinit var backButton: ImageButton
    private lateinit var partidas: List<PlayerScore>
    private lateinit var adapter: TopGlobalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.top_global)
        backButton = findViewById(R.id.back_button)
        adapter = TopGlobalAdapter(listOf())
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTop)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        obtenerTopTen(recyclerView)

    }

    fun obtenerTopTen(recyclerView: RecyclerView) {
        firebaseApi.getTopScores().enqueue(object : Callback<Map<String, PlayerScore>> {
            override fun onResponse(
                call: Call<Map<String, PlayerScore>>,
                response: Response<Map<String, PlayerScore>>
            ) {
                if (response.isSuccessful) {
                    val lista = response.body()?.values?.toList() ?: emptyList()
                    val topTen = lista.sortedByDescending { it.coinDif }.take(10)

                    recyclerView.adapter = TopGlobalAdapter(topTen)

                }
            }

            override fun onFailure(call: Call<Map<String, PlayerScore>>, t: Throwable) {
                Log.e("Firebase", "Error al obtener puntuaciones", t)
            }
        })
    }
}